package com.faas.common;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
    private UnboundedBuffer<Runnable> taskQueue;
    private List<ThreadBehaviour> threads;
    private AtomicBoolean executing;

    public ThreadPool() {
        taskQueue = new UnboundedBuffer<>();
        threads = new ArrayList<>();
        executing = new AtomicBoolean(false);
    }

    public void start(int numThreads) {
        this.executing.set(true);
        for (int i = 0; i < numThreads; i++) {
            ThreadBehaviour thread = new ThreadBehaviour("<Thread-" + i + ">", executing, taskQueue);
            threads.add(thread);
            thread.start();
        }
    }

    public void execute(Runnable task) {
        if (this.executing.get()) {
            this.taskQueue.produce(task);
        } else {
            throw new IllegalStateException("ThreadPool is stopped");
        }
    }

    public void stop() {
        if (!this.executing.get()) {
            System.out.println("ThreadPool is already stopped");
            return;
        }

        this.executing.set(false);
        System.out.println("ThreadPool is stopping");
        this.taskQueue.notifyWaiters();
        for (ThreadBehaviour thread : this.threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("Error stop: " + e);
            }
        }
    }
}
