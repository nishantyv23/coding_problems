package main

import (
	"context"
	"fmt"
	"time"

	"backupqueue"
)

func main() {

	// 3 workers
	// Maximum 10 jobs waiting in the queue.
	q := backupqueue.NewQueue(
		3,
		10,
		func(job backupqueue.Job) backupqueue.Result {

			fmt.Printf(
				"Processing job=%s priority=%d\n",
				job.ID,
				job.Priority,
			)

			// Simulate backup work.
			time.Sleep(500 * time.Millisecond)

			return backupqueue.Result{
				JobID:  job.ID,
				Output: []byte("backup completed"),
			}
		},
	)

	// Submit jobs.
	for i := 0; i < 15; i++ {

		job := backupqueue.Job{
			ID:       fmt.Sprintf("job-%d", i),
			Priority: i % 5,
			Payload:  []byte("backup data"),
		}

		err := q.Submit(job)

		if err != nil {
			fmt.Printf(
				"Failed to submit %s: %v\n",
				job.ID,
				err,
			)
		}
	}

	// Consume results.
	go func() {
		for result := range q.Results() {

			if result.Err != nil {
				fmt.Printf(
					"Job %s failed: %v\n",
					result.JobID,
					result.Err,
				)
				continue
			}

			fmt.Printf(
				"Job %s completed: %s\n",
				result.JobID,
				string(result.Output),
			)
		}
	}()

	// Graceful shutdown.
	ctx, cancel := context.WithTimeout(
		context.Background(),
		10*time.Second,
	)
	defer cancel()

	if err := q.Shutdown(ctx); err != nil {
		fmt.Println("Shutdown:", err)
	}
}
