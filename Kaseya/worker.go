package backupqueue

import "container/heap"

func (q *queue) dispatcher() {
	defer q.dispatchWG.Done()

	pq := &priorityQueue{}
	heap.Init(pq)

	for {
		var (
			nextJob Job
			workOut chan Job
		)

		/*
			If the priority queue has a job,enable the workCh case.
			If the queue is empty, workOut is nil,so that select case is disabled.
		*/
		if pq.Len() > 0 {
			nextJob = pq.peekJob()
			workOut = q.workCh
		}

		select {

		// Shutdown signal.
		case <-q.done:

			/*
				Shutdown means:
				1. Stop accepting new jobs.
				2. Dispatch everything already accepted.
				3. Close workCh.
				4. Workers finish remaining jobs.
			*/

			for pq.Len() > 0 {
				job := pq.popJob()
				q.workCh <- job
			}

			close(q.workCh)

			return

		// New submission.
		case req := <-q.submitCh:

			if q.closed.Load() {
				req.resp <- ErrShutdown
				continue
			}

			if pq.Len() >= q.capacity {
				req.resp <- ErrQueueFull
				continue
			}

			pq.pushJob(req.job)

			// Tell Submit() that the job was accepted.
			req.resp <- nil

		// Send highest-priority job to a worker.
		case workOut <- nextJob:

			pq.popJob()
		}
	}
}

func (q *queue) worker() {
	defer q.workerWG.Done()

	for job := range q.workCh {
		result := q.handler(job)
		q.results <- result
	}
}
