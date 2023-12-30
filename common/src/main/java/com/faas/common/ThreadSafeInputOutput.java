package com.faas.common;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeInputOutput {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Scanner scanner = new Scanner(System.in);

    public static void printString(String s){
        lock.lock();
        try {
            System.out.println(s);
        } finally {
            lock.unlock();
        }
    }

    public static Queue<String> getInput(int nLines){
        lock.lock();
        try {
            ArrayDeque<String> inputs = new ArrayDeque<>();
            for (int i = 0; i < nLines; i++) {
                String line = scanner.next();
                inputs.add(line);
            }
            return inputs;
        } finally {
            lock.unlock();
        }
    }
}
