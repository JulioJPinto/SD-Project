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
    private ThreadPool threadPool;
    private Socket socket;
    private Demultiplexer conn;
    private ReturnListener listener;
    private AtomicInteger jobCounter;

    public Client(ReturnListener listener) throws IOException {
        this.socket = new Socket(SERVER_MANAGER_ADDRESS, SERVER_MANAGER_PORT);
        this.conn = new Demultiplexer(new TaggedConnection(this.socket));
        this.conn.start();
        this.threadPool = new ThreadPool();
        this.threadPool.start(4);
        this.listener = listener;
        this.jobCounter = new AtomicInteger(0);
    }

    public void closeClient() throws Exception {
        this.conn.close();
        this.threadPool.stop();
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

    public void executeJob(String filename, int memoryNeeded){
        threadPool.execute(()->{
            jobCounter.increment();
            int jobID = jobCounter.get();

            listener.onStringReceived("A enviar job nº " + jobID);

            boolean success = false;
            try {
                success = this.sendRequest(filename,memoryNeeded,jobID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (success)
                listener.onStringReceived("Job n " + jobID + " com input no ficheiro: " + filename + " do cliente n " + authenticatedClientID + " executado com sucesso.");
            else
                listener.onStringReceived("Job n " + jobID + " com input no ficheiro: " + filename + " do cliente n " + authenticatedClientID + " falhou");
        });
    }

    private boolean sendRequest(String filename, int memoryNeeded, int jobID) throws IOException {

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

    public void executeStatus(){
        threadPool.execute(()->{
            String res;
            try {
                StatusResponse status = this.getStatus();
                listener.onStringReceived("Atualmente existem " + status.getPendingTasks() + " tarefas pendentes e a memória disponível é " + status.getAvailableMemory() + ".");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private StatusResponse getStatus() throws IOException {
        StatusRequest statusReq = new StatusRequest(authenticatedClientID);

        conn.send(Thread.currentThread().getId(),statusReq);

        return (StatusResponse) conn.receive(Thread.currentThread().getId());
    }
}