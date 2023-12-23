package com.faas.common;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class BoundedBuffer<T> {
    private final ReentrantLock lock;
    private final Condition notFull;
    private final Condition notEmpty;
    private final T[] items;
    private final int capacity;
    private int head, tail, count;

    @SuppressWarnings("unchecked") // suppress unchecked cast warning on generic array creation
    public BoundedBuffer(int capacity) {
        this.lock = new ReentrantLock();
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
        this.items = (T[]) new Object[capacity];
        this.capacity = capacity;
        this.head = this.tail = this.count = 0;
    }

    private boolean isEmpty() {
        return count == 0;
    }

    private boolean isFull() {
        return count == capacity;
    }

    public void produce(T item) throws InterruptedException {
        this.lock.lock();
        try {
            while (isFull()) {
                this.notFull.await();
            }
            this.items[this.tail] = item;
            this.tail = (this.tail + 1) % this.capacity;
            this.count++;
            this.notEmpty.signal();
        } finally {
            this.lock.unlock();
        }
    }

    public T consume() throws InterruptedException {
        this.lock.lock();
        try {
            while (isEmpty()) {
                this.notEmpty.await();
            }
            T item = this.items[this.head];
            this.items[this.head] = null;
            this.head = (this.head + 1) % this.capacity;
            this.count--;
            this.notFull.signal();
            return item;
        } finally {
            this.lock.unlock();
        }
    }
}
