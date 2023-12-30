package com.faas.client;

import com.faas.common.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class Client {
    private static final String SERVER_MANAGER_ADDRESS = "localhost"; // 127.0.0.1
    private static final int SERVER_MANAGER_PORT = 8080;
    private int authenticatedClientID;



    private Socket socket;
    private Demultiplexer conn;

    public Client() throws IOException {
        this.socket = new Socket(SERVER_MANAGER_ADDRESS, SERVER_MANAGER_PORT);
        this.conn = new Demultiplexer(new TaggedConnection(this.socket));
        this.conn.start();
    }

    public void closeClient() throws Exception {
        this.conn.close();
    }

    public int loginUser(String username, String password) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        AuthenticationRequest loginReq = new AuthenticationRequest(1,username,password);

        conn.send(Thread.currentThread().getId(),loginReq);

        AuthenticationResponse loginResp = (AuthenticationResponse) conn.receive(Thread.currentThread().getId());

        System.out.println(loginResp.toString());

        this.authenticatedClientID = loginResp.getAuthenticatedClientID();

        return loginResp.getAuthenticatedClientID();
    }
    public int registerNewUser(String username, String password) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        AuthenticationRequest newUserReq = new AuthenticationRequest(2,username,password);

        conn.send(Thread.currentThread().getId(),newUserReq);

        AuthenticationResponse newUserResp = (AuthenticationResponse) conn.receive(Thread.currentThread().getId());

        System.out.println(newUserResp.toString());

        this.authenticatedClientID = newUserResp.getAuthenticatedClientID();

        return newUserResp.getAuthenticatedClientID();
    }

    public boolean sendRequest(String filename, int memoryNeeded, int jobID) throws IOException {

        boolean success = true;

        Path inputPath = Path.of("client","Inputs",filename);

        byte[] input = Files.readAllBytes(inputPath);

        ExecuteRequest toSend = new ExecuteRequest(authenticatedClientID,input,memoryNeeded);

        conn.send(Thread.currentThread().getId(),toSend);

        ExecuteResponse response = (ExecuteResponse) conn.receive(Thread.currentThread().getId());

        if (!response.isSuccess())
            success = false;

        Path outputPath = Path.of("client", "Outputs", "resultado cliente " + authenticatedClientID + " jobID " + jobID + " input " + filename + ".7z");
        Files.write(outputPath, response.getResult());

        return success;
    }

    public StatusResponse getStatus() throws IOException {
        StatusRequest statusReq = new StatusRequest(authenticatedClientID);

        conn.send(Thread.currentThread().getId(),statusReq);

        return (StatusResponse) conn.receive(Thread.currentThread().getId());
    }
}