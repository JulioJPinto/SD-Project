package com.faas.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AuthenticationResponse extends Message{

    private int authenticatedClientID;

    public AuthenticationResponse(){
        super(0,AuthenticationResponse.class.getName());
        this.authenticatedClientID = 0;
    }
    public AuthenticationResponse(int clientID){
        super(clientID,AuthenticationResponse.class.getName());
        this.authenticatedClientID = clientID;

    }

    public int getAuthenticatedClientID(){
        return authenticatedClientID;
    }

    @Override
    protected void serializeSubclass(DataOutputStream out) throws IOException {
        out.writeInt(authenticatedClientID);
    }

    @Override
    protected Message deserializeSubclass(DataInputStream in, int clientID) throws IOException {
        int clientIDread = in.readInt();

        return new AuthenticationResponse(clientIDread);
    }

    @Override
    public String toString() {
        return super.toString() + "AuthenticationResponse{" +
                "authenticatedClientID=" + authenticatedClientID +
                '}';
    }
}
