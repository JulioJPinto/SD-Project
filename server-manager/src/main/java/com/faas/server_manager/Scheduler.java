package com.faas.server_manager;

import com.faas.common.*;
import java.util.Random;

public class Scheduler {
    private static final UnboundedBuffer<Tuple<Message, Integer>> skippedTasks = new UnboundedBuffer<>();
    private static final Random random = new Random();

    public static WorkerStats chooseNextWorker(Message task) {
        WorkerStats nextWorker = null;
        ExecuteRequest nextTask = null;
        int skippedTimes;

        if (!skippedTasks.isEmpty()) {
            for (Tuple<Message, Integer> skippedTask : skippedTasks.copyToList()) {
                nextTask = (ExecuteRequest) skippedTask.getFirst();
                skippedTimes = skippedTask.getSecond();
                if (skippedTimes == 3) {
                    return forceExecuteTask(nextTask);
                } else {
                    nextWorker = tryExecuteTask(nextTask, skippedTimes);
                    if (nextWorker != null) {
                        return nextWorker;
                    }
                }
            }
        }

        nextTask = (ExecuteRequest) task;
        skippedTimes = 0;

        if (!WorkersInfo.possibleExecution(nextTask.getMemoryNeeded())) {
            System.out.println("Task dropped");
            return null;
        }

        return tryExecuteTask(nextTask, skippedTimes);
    }

    public static WorkerStats tryExecuteTask(ExecuteRequest task, int skippedTimes) {
        WorkerStats chosenServer = null;
        Tuple<int[], Integer> availableServers = WorkersInfo.getAvailableWorkers(task.getMemoryNeeded());

        if(availableServers.getSecond() > 0) {
            int randomIndex = random.nextInt(availableServers.getSecond());
            int workerId = availableServers.getFirst()[randomIndex];
            WorkersInfo.updateWorkerMemory(workerId, task.getMemoryNeeded(), true);
            chosenServer = WorkersInfo.getWorker(workerId);
        } else {
            skippedTasks.produce(new Tuple<Message, Integer>(task, skippedTimes + 1));
        }

        return chosenServer;
    }

    public static WorkerStats forceExecuteTask(ExecuteRequest task) {
        boolean executed = false;
        WorkerStats chosenServer = null;

        while (!executed) {
            int workerId = WorkersInfo.getFirstAvailableServer(task.getMemoryNeeded());
            if (workerId != -1) {
                WorkersInfo.updateWorkerMemory(workerId, task.getMemoryNeeded(), true);
                chosenServer = WorkersInfo.getWorker(workerId);
                executed = true;
            }
        }

        return chosenServer;
    }
}
