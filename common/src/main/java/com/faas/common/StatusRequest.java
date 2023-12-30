package com.faas.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatusRequest extends Message{

    public StatusRequest(){
        super(0,StatusRequest.class.getName());
    }

    public StatusRequest(int clientID){
        super(clientID,StatusRequest.class.getName());
    }

    @Override
    protected void serializeSubclass(DataOutputStream out) throws IOException {

    }

    @Override
    protected Message deserializeSubclass(DataInputStream in, int clientID) throws IOException {
        return new StatusRequest(clientID);
    }
}
