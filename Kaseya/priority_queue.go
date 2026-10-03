package backupqueue

import "container/heap"

type pqItem struct {
	job Job
}

type priorityQueue []*pqItem

func (pq priorityQueue) Len() int {
	return len(pq)
}

// Higher priority comes first.
func (pq priorityQueue) Less(i, j int) bool {
	return pq[i].job.Priority > pq[j].job.Priority
}

func (pq priorityQueue) Swap(i, j int) {
	pq[i], pq[j] = pq[j], pq[i]
}

func (pq *priorityQueue) Push(x interface{}) {
	item := x.(*pqItem)
	*pq = append(*pq, item)
}

func (pq *priorityQueue) Pop() interface{} {
	old := *pq
	n := len(old)

	item := old[n-1]

	// Avoid retaining the object unnecessarily.
	old[n-1] = nil

	*pq = old[:n-1]

	return item
}

func (pq *priorityQueue) pushJob(job Job) {
	heap.Push(pq, &pqItem{
		job: job,
	})
}

func (pq *priorityQueue) popJob() Job {
	item := heap.Pop(pq).(*pqItem)
	return item.job
}

func (pq *priorityQueue) peekJob() Job {
	return (*pq)[0].job
}
