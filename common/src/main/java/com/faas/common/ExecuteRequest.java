package com.faas.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExecuteRequest extends Message{

    private byte[] input;

    private int memoryNeeded;

    public ExecuteRequest(){
        super(0,ExecuteRequest.class.getName());
        this.input = null;
        this.memoryNeeded = 0;
    }

    public ExecuteRequest(int client_id, byte[] input, int memoryNeeded){
        super(client_id,ExecuteRequest.class.getName());
        this.input = input;
        this.memoryNeeded = memoryNeeded;
    }

    public byte[] getInput() {
        return input;
    }

    public int getMemoryNeeded() {
        return memoryNeeded;
    }

    @Override
    protected void serializeSubclass(DataOutputStream out) throws IOException {
        out.writeInt(input.length);
        out.write(this.input);
        out.writeInt(this.memoryNeeded);
    }

    @Override
    protected Message deserializeSubclass(DataInputStream in, int clientID) throws IOException {
        int length = in.readInt();
        byte[] input = in.readNBytes(length);
        int memoryNeeded = in.readInt();

        return new ExecuteRequest(clientID,input,memoryNeeded);
    }
}
