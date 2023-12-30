package com.faas.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatusResponse extends Message{

    private int pendingTasks;

    private int availableMemory;

    public StatusResponse(){
        super(0,StatusResponse.class.getName());
        this.pendingTasks = 0;
        this.availableMemory = 0;
    }

    public StatusResponse(int clientID, int pendingTasks, int availableMemory){
        super(clientID,StatusResponse.class.getName());
        this.pendingTasks = pendingTasks;
        this.availableMemory = availableMemory;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    public int getAvailableMemory() {
        return availableMemory;
    }

    @Override
    protected void serializeSubclass(DataOutputStream out) throws IOException {
        out.writeInt(this.pendingTasks);
        out.writeInt(this.availableMemory);
    }

    @Override
    protected Message deserializeSubclass(DataInputStream in, int clientID) throws IOException {
        int pendingTasks = in.readInt();
        int availableMemory = in.readInt();

        return new StatusResponse(clientID,pendingTasks,availableMemory);
    }
}
