package Onehouse;
import java.util.*;

class Task
{
    int id;
    boolean complete;
    TaskOrchestration to;

    Task(int id)
    {
        this.id = id;
        complete = false;
        to = new TaskOrchestration();
    }

    void run()
    {
        System.out.println("Task scheduled and running");
        complete();
    }

    void complete()
    {
        this.complete = true;
        to.check(this.id);
    }
}

class TaskOrchestration
{
    Map<Integer, Task> taskDepMap = new HashMap<>();
    Map<Integer, List<Integer>> reverseLookupMap = new HashMap<>();

    void addTask(Task t)
    {
        if(!taskDepMap.containsKey(t.id))
        {
            taskDepMap.putIfAbsent(t.id, t);
        }
        else
        {
            System.out.println("Task already exists with this id");
        }

    }
    void addDependency(int id1, int id2)
    {
        //taskDepMap.get().add(id2);
        if(reverseLookupMap.containsKey(id2))
            reverseLookupMap.get(id2).add(id1);
        else
        {
            List<Integer> list = new ArrayList<>();
            list.add(id1);
            reverseLookupMap.put(id1, list);
        }
    }

    void check(int checkID)
    {
        List<Integer> tasksList = reverseLookupMap.get(checkID);
        for(Integer id : tasksList)
        {
            if(taskDepMap.get(id).complete)
                taskDepMap.get(checkID).run();
        }
    }

    void executeWorkflow()
    {
        for(Integer tid:reverseLookupMap.keySet())
        {
            List<Integer> deps = reverseLookupMap.get(tid);
            if(deps.isEmpty())
            {
                taskDepMap.get(tid).run();
            }
            else
            {
                for(Integer id:deps)
                {
                    if(taskDepMap.get(id).complete)
                        taskDepMap.get(tid).run();
                }
            }
        }
    }

    void shutdown()
    {
        for(Integer tid:taskDepMap.keySet())
        {
            if(!taskDepMap.get(tid).complete)
                taskDepMap.get(tid).run();
        }

        System.out.println("All tasks completed successfully. Shutting down");
    }
}

public class WorkflowOrchestration
{
    public static void main(String args[])
    {
        TaskOrchestration to = new TaskOrchestration();
        Task t1 = new Task(1);
        Task t2 = new Task(2);
        Task t3 = new Task(3);

        to.addTask(t1);
        to.addTask(t2);
        to.addTask(t3);

        to.addDependency(t1.id, t2.id);
        to.addDependency(t2.id, t3.id);

        to.executeWorkflow();

        to.shutdown();
    }
} 