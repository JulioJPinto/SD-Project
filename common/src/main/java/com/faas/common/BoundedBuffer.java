package com.faas.common;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BoundedBuffer<T> {
    private final ReentrantReadWriteLock lock;
    private final Condition notFull;
    private final Condition notEmpty;
    private final T[] items;
    private final int capacity;
    private int head, tail, count;

    @SuppressWarnings("unchecked") // suppress unchecked cast warning on generic array creation
    public BoundedBuffer(int capacity) {
        this.lock = new ReentrantReadWriteLock();
        this.notFull = lock.writeLock().newCondition();
        this.notEmpty = lock.writeLock().newCondition();
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

    public int length(){
        this.lock.readLock().lock();
        try {
            return this.count;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void produce(T item) throws InterruptedException {
        this.lock.writeLock().lock();
        try {
            while (isFull()) {
                this.notFull.await();
            }
            this.items[this.tail] = item;
            this.tail = (this.tail + 1) % this.capacity;
            this.count++;
            this.notEmpty.signal();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public T consume() throws InterruptedException {
        this.lock.writeLock().lock();
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
            this.lock.writeLock().unlock();
        }
    }

    public T peek(){
        this.lock.readLock().lock();
        try {
            return this.items[this.head];
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void remove(T item) {
        this.lock.writeLock().lock();
        try {
            if (this.head < this.tail) {
                for (int i = this.head; i < this.tail; i++) {
                    if (this.items[i].equals(item)) {
                        shift(i, false, false);
                        this.notFull.signal();
                        return;
                    }
                }
            } else {
                for (int i = this.head; i < this.capacity; i++) {
                    if (this.items[i].equals(item)) {
                        shift(i, true, false);
                        this.notFull.signal();
                        return;
                    }
                }
                for (int i = 0; i < this.tail; i++) {
                    if (this.items[i].equals(item)) {
                        shift(i, true, true);
                        this.notFull.signal();
                        return;
                    }
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void shift(int index, boolean split, boolean right) { // index, isSplitted, onlyRight
        if (!split || right) { // not split or (split and right)
            for (int i = index; i < this.tail; i++) {
                this.items[i] = this.items[i + 1];
            }
        } else { // split in left and right
            for (int i = index; i < this.capacity - 1; i++) {
                this.items[i] = this.items[i + 1];
            }

            this.items[this.capacity - 1] = this.items[0];

            for (int i = 0; i < this.tail; i++) {
                this.items[i] = this.items[i + 1];
            }
        }
        this.items[this.tail] = null;
        this.tail--;

        if (this.tail < 0) {
            this.tail = this.capacity - 1;
        }
        this.count--;
    }
}
