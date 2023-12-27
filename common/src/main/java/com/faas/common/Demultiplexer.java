package com.faas.common;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable {

    //Existe um bug em que por vezes o servidor envia pacotes com uma tag que ainda não existe nos mapas
    //isto faz com que o pacote seja dropado e depois a thread não recebe nada
    //adicionar um pequeno sleep no servidor (acredito que fosse ser equivalente a processamento feito sobre uma mensagem a sério) tem funcionado sem problemas
    //no entanto, não sei até que ponto é que dizer que o servidor é demasiado rápido neste caso em específico é uma boa justificação para isto estar tudo a funcionar sem qualquer problema
    private TaggedConnection conn;

    private ReentrantLock lock;

    private Map<Long,Condition> conditions;

    private Map<Long, Deque<Message>> messageQueues;

    private Exception exception;
    public Demultiplexer(TaggedConnection conn){
        this.conn = conn;
        this.lock = new ReentrantLock();
        this.conditions = new HashMap<>();
        this.messageQueues = new HashMap<>();
        this.exception = null;
    }

    public void start(){
        new Thread(() ->{
            try {
                while (true) {
                    TaggedConnection.Frame receivedFrame = this.conn.receive();

                    this.lock.lock();
                    try {
                        long tag = receivedFrame.getTag();

                        if(!this.messageQueues.containsKey(tag))
                            continue;

                        Deque<Message> buf = this.messageQueues.get(tag);
                        buf.add(receivedFrame.getMessage());
                        this.conditions.get(tag).signal();

                    } finally {
                        this.lock.unlock();
                    }
                }
            } catch (IllegalAccessException | ClassNotFoundException | InvocationTargetException |
                     NoSuchMethodException | InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IOException e){
                this.lock.lock();
                try {
                    this.exception = e;
                    this.conditions.forEach((tag,cond) -> cond.signalAll());
                }finally {
                    this.lock.unlock();
                }
            }
        }).start();
    }

    public void send(long tag, Message message) throws IOException {
        this.conn.send(tag,message);
    }

    public Message receive(long tag){
        this.lock.lock();

        try{
            if (!this.conditions.containsKey(tag)){
                this.conditions.put(tag, this.lock.newCondition());
                this.messageQueues.put(tag, new ArrayDeque<>());
            }

            Condition cond = this.conditions.get(tag);
            Deque<Message> buf = this.messageQueues.get(tag);

            while(buf.isEmpty() && this.exception == null){
                cond.await();
            }

            if (!buf.isEmpty())
                return buf.poll();
            else
                throw this.exception;

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        this.conn.close();
    }
}
