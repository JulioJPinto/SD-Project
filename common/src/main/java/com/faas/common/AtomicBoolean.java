package com.faas.common;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AtomicBoolean {
    private boolean value;
    private ReentrantReadWriteLock lock;

    public AtomicBoolean(boolean value) {
        this.value = value;
        this.lock = new ReentrantReadWriteLock();
    }

    public boolean get() {
        this.lock.readLock().lock();
        try {
            return this.value;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void set(boolean value) {
        this.lock.writeLock().lock();
        try {
            this.value = value;
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
