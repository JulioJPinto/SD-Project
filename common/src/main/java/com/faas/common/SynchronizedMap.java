package com.faas.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SynchronizedMap<K, V> {

    private final Map<K, V> map;
    private final ReadWriteLock readWriteLock;

    public SynchronizedMap(){
        this.map = new HashMap<>();
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    public V get(K key) {
        readWriteLock.readLock().lock();
        try {
            return map.get(key);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void put(K key, V value) {
        readWriteLock.writeLock().lock();
        try {
            map.put(key, value);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public boolean containsKey(K key) {
        readWriteLock.readLock().lock();
        try {
            return map.containsKey(key);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public Set<Map.Entry<K,V>> entrySet(){
        readWriteLock.readLock().lock();
        try {
            return map.entrySet();
        }finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void remove(K key) {
        readWriteLock.writeLock().lock();
        try {
            map.remove(key);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public int size() {
        readWriteLock.readLock().lock();
        try {
            return map.size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
