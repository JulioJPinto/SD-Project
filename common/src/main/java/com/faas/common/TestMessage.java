package com.faas.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TestMessage extends Message{
    //apenas para testar o demultiplexer
    private String s;

    public TestMessage(int clientID, String s){
        super(clientID,TestMessage.class.getName());
        this.s = s;
    }
    public TestMessage(){
        super();
        this.s = null;
    }

    public String getS(){
        return s;
    }
    @Override
    protected void serializeSubclass(DataOutputStream out) throws IOException {
        out.writeUTF(s);
    }

    @Override
    protected Message deserializeSubclass(DataInputStream in, int clientID) throws IOException {
        String rs = in.readUTF();
        return new TestMessage(clientID,rs);
    }
}
