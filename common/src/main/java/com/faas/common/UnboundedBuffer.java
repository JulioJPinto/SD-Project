package com.faas.common;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Condition;

public class UnboundedBuffer<T> {
    private final Queue<T> queue;
    private final ReentrantReadWriteLock lock;
    private final Condition notEmpty;

    public UnboundedBuffer() {
        this.queue = new ArrayDeque<T>();
        this.lock = new ReentrantReadWriteLock();
        this.notEmpty = lock.writeLock().newCondition();
    }

    public boolean isEmpty() {
        this.lock.readLock().lock();
        try {
            return this.queue.isEmpty();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void produce(T item) {
        this.lock.writeLock().lock();
        try {
            this.queue.add(item);
            this.notEmpty.signal();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public T consume() throws InterruptedException {
        this.lock.writeLock().lock();
        try {
            while (this.queue.isEmpty()) {
                this.notEmpty.await();
            }
            return this.queue.poll();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public T peek() {
        this.lock.readLock().lock();
        try {
            return this.queue.peek();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public int size() {
        this.lock.readLock().lock();
        try {
            return this.queue.size();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void notifyWaiters() {
        this.lock.writeLock().lock();
        try {
            this.notEmpty.signalAll();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public List<T> copyToList() {
        this.lock.writeLock().lock();
        try {
            return List.copyOf(this.queue);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}