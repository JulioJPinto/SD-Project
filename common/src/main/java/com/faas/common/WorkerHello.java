package com.faas.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class WorkerHello extends Message{

    int totalMemory;

    public WorkerHello(){
        super(0,WorkerHello.class.getName());
        this.totalMemory = 0;
    }

    public WorkerHello(int totalMemory){
        super(0,WorkerHello.class.getName());
        this.totalMemory = totalMemory;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    @Override
    protected void serializeSubclass(DataOutputStream out) throws IOException {
        out.writeInt(totalMemory);
    }

    @Override
    protected Message deserializeSubclass(DataInputStream in, int clientID) throws IOException {
        int totalMemory = in.readInt();

        return new WorkerHello(totalMemory);
    }
}
