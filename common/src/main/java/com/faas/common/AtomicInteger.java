package com.faas.common;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AtomicInteger {
    private int value;
    private ReentrantReadWriteLock lock;

    public AtomicInteger(int value) {
        this.value = value;
        this.lock = new ReentrantReadWriteLock();
    }

    public int get() {
        this.lock.readLock().lock();
        try {
            return this.value;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void set(int value) {
        this.lock.writeLock().lock();
        try {
            this.value = value;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void increment() {
        this.lock.writeLock().lock();
        try {
            this.value++;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void decrement() {
        this.lock.writeLock().lock();
        try {
            this.value--;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void add(int value){
        this.lock.writeLock().lock();
        try {
            this.value += value;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void subtract(int value){
        this.lock.writeLock().lock();
        try {
            this.value -= value;
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}