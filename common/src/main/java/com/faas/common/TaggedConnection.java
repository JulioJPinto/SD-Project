package com.faas.common;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {

    public static class Frame {
        private final long tag;
        private final Message message;

        public Frame(long tag, Message message) {
            this.tag = tag;
            this.message = message;
        }

        public long getTag(){
            return this.tag;
        }

        public Message getMessage(){
            return this.message;
        }

    }

    private Socket socket;
    private DataInputStream in;
    private ReentrantLock readLock;
    private DataOutputStream out;
    private ReentrantLock writeLock;

    public TaggedConnection(Socket socket) throws IOException {
        this.socket = socket;

        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.readLock = new ReentrantLock();

        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.writeLock = new ReentrantLock();
    }

    public void send(Frame frame) throws IOException {
        this.send(frame.tag, frame.message);
    }

    public void send(long tag, Message data) throws IOException {
        this.writeLock.lock();
        try {
            this.out.writeLong(tag);
            data.serialize(out);
            this.out.flush();
        } finally {
            this.writeLock.unlock();
        }
    }

    public Frame receive() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.readLock.lock();
        try {
            long tag = this.in.readLong();
            Message message = Message.deserialize(in);
            return new Frame(tag, message);
        } finally {
            this.readLock.unlock();
        }
    }

    public void close() throws IOException {
        this.in.close();
        this.out.close();
        this.socket.close();
    }
}