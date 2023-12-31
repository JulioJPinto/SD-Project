package com.faas.server_manager;

import com.faas.common.*;
import java.util.Map;
import java.util.Set;

public class WorkersInfo {
    private static AtomicInteger idFactory = new AtomicInteger(0);
    private static AtomicInteger availableMemory = new AtomicInteger(0);
    private static final SynchronizedMap<Integer, WorkerStats> workers = new SynchronizedMap<>();


    private static void updateAvailableMemory(int memory, boolean subtract) {
        if (subtract) {
            availableMemory.subtract(memory);
        } else {
            availableMemory.add(memory);
        }
    }

    public static int getAvailableMemory() {
        return availableMemory.get();
    }

    public static void updateWorkerMemory(int id, int memory, boolean subtract) {
        WorkerStats worker = workers.get(id);
        worker.update(memory, subtract);
        updateAvailableMemory(memory, subtract);
    }

    public static int generateId() {
        idFactory.increment();
        return idFactory.get();
    }

    public static Set<Map.Entry<Integer, WorkerStats>> getWorkersEntries() {
        return workers.entrySet();
    }

    public static void addWorker(int id, WorkerStats worker) {
        workers.put(id, worker);
    }

    public static void removeWorker(int id) {
        workers.remove(id);
    }

    public static WorkerStats getWorker(int id) {
        return workers.get(id);
    }

    public static boolean possibleExecution(int memory) {
        for (Map.Entry<Integer, WorkerStats> entry : workers.entrySet()) {
            if (entry.getValue().getTotalMemory() >= memory) {
                return true;
            }
        }
        return false;
    }

    public static Tuple<int[], Integer> getAvailableWorkers(int space) {
        int[] availableWorkers = new int[workers.size()];
        int index = 0;
        for (Map.Entry<Integer, WorkerStats> entry : workers.entrySet()) {
            if (entry.getValue().getOccupiedMemory() + space <= entry.getValue().getTotalMemory()) {
                availableWorkers[index] = entry.getKey();
            }
        }
        return new Tuple<>(availableWorkers, index);
    }

    public static int getFirstAvailableServer(int space) {
        for (Map.Entry<Integer, WorkerStats> entry : workers.entrySet()) {
            if (entry.getValue().getOccupiedMemory() + space <= entry.getValue().getTotalMemory()) {
                return entry.getKey();
            }
        }

        return -1;
    }

}
