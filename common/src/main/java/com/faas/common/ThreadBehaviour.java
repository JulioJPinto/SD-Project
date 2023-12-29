package com.faas.common;

public class ThreadBehaviour extends Thread {
    private AtomicBoolean executing;
    private UnboundedBuffer<Runnable> taskQueue;

    public ThreadBehaviour(String name, AtomicBoolean executing, UnboundedBuffer<Runnable> taskQueue) {
        super(name);
        this.executing = executing;
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        try {
            while (this.executing.get()) {
                while (!this.taskQueue.isEmpty()) {
                    Runnable task = this.taskQueue.consume();
                    task.run();
                }
            }
        } catch (RuntimeException | InterruptedException e) {
            System.out.println("Error run: " + e);
        }
    }
}
