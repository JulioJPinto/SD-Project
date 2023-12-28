package com.faas.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExecuteResponse extends Message{

    private byte[] result;

    public ExecuteResponse(){
        super(0,ExecuteResponse.class.getName());
        this.result = null;
    }

    public ExecuteResponse(int clientID, byte[] result){
        super(clientID,ExecuteResponse.class.getName());
        this.result = result;
    }

    public byte[] getResult() {
        return result;
    }

    @Override
    protected void serializeSubclass(DataOutputStream out) throws IOException {
        out.writeInt(this.result.length);
        out.write(this.result);
    }

    @Override
    protected Message deserializeSubclass(DataInputStream in, int clientID) throws IOException {
        int length = in.readInt();
        byte[] result = in.readNBytes(length);

        return new ExecuteResponse(clientID,result);
    }
}
