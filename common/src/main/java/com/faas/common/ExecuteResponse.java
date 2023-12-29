package com.faas.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExecuteResponse extends Message{

    private boolean success;
    private byte[] result;

    public ExecuteResponse(){
        super(0,ExecuteResponse.class.getName());
        this.success = false;
        this.result = null;
    }

    public ExecuteResponse(int clientID, boolean success, byte[] result){
        super(clientID,ExecuteResponse.class.getName());
        this.success = success;
        this.result = result;
    }

    public byte[] getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    protected void serializeSubclass(DataOutputStream out) throws IOException {
        out.writeBoolean(this.success);
        out.writeInt(this.result.length);
        out.write(this.result);
    }

    @Override
    protected Message deserializeSubclass(DataInputStream in, int clientID) throws IOException {
        boolean success = in.readBoolean();
        int length = in.readInt();
        byte[] result = in.readNBytes(length);

        return new ExecuteResponse(clientID,success,result);
    }
}
