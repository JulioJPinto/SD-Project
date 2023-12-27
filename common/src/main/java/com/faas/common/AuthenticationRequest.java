package com.faas.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AuthenticationRequest extends Message{

    private int type; // 1 - login, 2 - registar
    private String username;
    private String password;

    public AuthenticationRequest(){
        super(0,AuthenticationRequest.class.getName());
        this.type = 0;
        this.username = "";
        this.password = "";
    }

    public AuthenticationRequest(int type,String username, String password){
        super(0,AuthenticationRequest.class.getName());
        this.type = type;
        this.username = username;
        this.password = password;
    }

    public int getType(){
        return this.type;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    @Override
    protected void serializeSubclass(DataOutputStream out) throws IOException {
        out.writeInt(this.type);
        out.writeUTF(this.username);
        out.writeUTF(this.password);
    }

    @Override
    protected Message deserializeSubclass(DataInputStream in, int clientID) throws IOException {
        int type = in.readInt();
        String username = in.readUTF();
        String password = in.readUTF();

        return new AuthenticationRequest(type,username,password);
    }

    @Override
    public String toString() {
        return super.toString() + "AuthenticationRequest{" +
                "type=" + type +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
