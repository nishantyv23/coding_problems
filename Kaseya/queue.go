package backupqueue

import (
	"context"
	"errors"
	"sync"
	"sync/atomic"
)

type Job struct {
	ID       string
	Priority int
	Payload  []byte
}

type Result struct {
	JobID  string
	Output []byte
	Err    error
}

type Queue interface {
	// Submit adds a job to the queue.
	// Returns an error if the queue is shut down or full.
	Submit(job Job) error

	// Results returns a channel containing completed job results.
	Results() <-chan Result

	// Shutdown stops accepting new jobs and waits for
	// already accepted jobs to finish.
	Shutdown(ctx context.Context) error
}

var (
	ErrShutdown  = errors.New("queue is shut down")
	ErrQueueFull = errors.New("queue is full")
)

type submitRequest struct {
	job  Job
	resp chan error
}

type queue struct {
	capacity int
	handler  func(Job) Result

	// submitCh is NEVER closed.
	// done is used to signal shutdown.
	submitCh chan submitRequest
	done     chan struct{}

	workCh  chan Job
	results chan Result

	closed atomic.Bool

	closeOnce sync.Once

	dispatchWG sync.WaitGroup
	workerWG   sync.WaitGroup
}

func NewQueue(
	workers int,
	capacity int,
	handler func(Job) Result,
) Queue {

	if workers <= 0 {
		panic("workers must be greater than 0")
	}

	if capacity <= 0 {
		panic("capacity must be greater than 0")
	}

	q := &queue{
		capacity: capacity,
		handler:  handler,

		submitCh: make(chan submitRequest),
		done:     make(chan struct{}),

		workCh: make(chan Job),

		// Buffering by number of workers prevents workers
		// from unnecessarily blocking on result delivery.
		results: make(chan Result, workers),
	}

	// Start dispatcher.
	q.dispatchWG.Add(1)
	go q.dispatcher()

	// Start workers.
	q.workerWG.Add(workers)

	for range workers {
		go q.worker()
	}

	return q
}

func (q *queue) Submit(job Job) error {

	// Fast path.
	if q.closed.Load() {
		return ErrShutdown
	}

	resp := make(chan error, 1)

	req := submitRequest{
		job:  job,
		resp: resp,
	}

	/*
		Important:
		We do NOT close submitCh during shutdown.
		Instead, done is closed to signal shutdown.
		This prevents:
		    panic: send on closed channel
	*/

	select {
	case <-q.done:
		return ErrShutdown

	case q.submitCh <- req:
	}

	// Dispatcher tells us whether the job was actually accepted.
	return <-resp
}

func (q *queue) Results() <-chan Result {
	return q.results
}

func (q *queue) Shutdown(ctx context.Context) error {

	q.closeOnce.Do(func() {
		q.closed.Store(true)
		close(q.done)
	})

	finished := make(chan struct{})

	go func() {
		// Dispatcher finishes first.
		q.dispatchWG.Wait()

		// Then all workers finish.
		q.workerWG.Wait()

		// Nobody can send results after workers finish.
		close(q.results)

		close(finished)
	}()

	select {
	case <-finished:
		return nil

	case <-ctx.Done():
		return ctx.Err()
	}
}
