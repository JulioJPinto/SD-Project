package com.faas.client;

import com.faas.common.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;


public class Client {
    private static final String SERVER_MANAGER_ADDRESS = "localhost"; // 127.0.0.1
    private static final int SERVER_MANAGER_PORT = 8080;

    private int authenticatedUserID;

    private Socket socket;
    private Demultiplexer conn;

    public Client() throws IOException {
        try {
            this.socket = new Socket(SERVER_MANAGER_ADDRESS, SERVER_MANAGER_PORT);
            this.conn = new Demultiplexer(new TaggedConnection(this.socket));
            this.conn.start();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void closeClient() throws IOException {
        try {
            this.conn.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public int loginUser(String username, String password) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        AuthenticationRequest loginReq = new AuthenticationRequest(1,username,password);

        conn.send(Thread.currentThread().getId(),loginReq);

        AuthenticationResponse loginResp = (AuthenticationResponse) conn.receive(Thread.currentThread().getId());

        System.out.println(loginResp.toString());

        this.authenticatedUserID = loginResp.getAuthenticatedClientID();

        return loginResp.getAuthenticatedClientID();
    }
    public int registerNewUser(String username, String password) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        AuthenticationRequest newUserReq = new AuthenticationRequest(2,username,password);

        conn.send(Thread.currentThread().getId(),newUserReq);

        AuthenticationResponse newUserResp = (AuthenticationResponse) conn.receive(Thread.currentThread().getId());

        System.out.println(newUserResp.toString());

        this.authenticatedUserID = newUserResp.getAuthenticatedClientID();

        return newUserResp.getAuthenticatedClientID();
    }

    public void sendMessage(String s) throws IOException {
        TestMessage toSend = new TestMessage(this.authenticatedUserID,s);

        conn.send(Thread.currentThread().getId(),toSend);
    }

    public Message receiveMessage() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return conn.receive(Thread.currentThread().getId());
    }

}