package com.faas.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public abstract class Message {

    private final int authClientID;

    private final String qualifiedSubclassName;

    public Message(){
        this.authClientID = 0;
        this.qualifiedSubclassName = "";
    }
    public Message(int authClientID, String subclassName){
        this.authClientID = authClientID;
        this.qualifiedSubclassName = subclassName;
    }

    public String getSubclassName(){
        return qualifiedSubclassName;
    }

    public int getAuthClientID(){
        return this.authClientID;
    }

    public void serialize(DataOutputStream out) throws IOException{
        out.writeInt(authClientID);
        out.writeUTF(qualifiedSubclassName);
        this.serializeSubclass(out);
    }

    public static Message deserialize(DataInputStream in) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        int authClientID = in.readInt();
        String subName = in.readUTF();
        Class<?> message = Class.forName(subName);
        Message castedMessage = (Message) message.getDeclaredConstructor().newInstance();

        return castedMessage.deserializeSubclass(in, authClientID);
    }

    protected abstract void serializeSubclass(DataOutputStream out) throws IOException;
    protected abstract Message deserializeSubclass(DataInputStream in, int clientID) throws IOException;

    @Override
    public String toString() {
        return "Message{" +
                "subclassName='" + qualifiedSubclassName + '\'' +
                '}';
    }
}
