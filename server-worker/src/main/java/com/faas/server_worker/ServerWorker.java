package com.faas.server_worker;

import com.faas.common.*;
import sd23.*;

import java.io.*;
import java.net.Socket;

public class ServerWorker {
    private static final String SERVER_MANAGER_ADDRESS = "localhost";
    private static final int SERVER_MANAGER_PORT = 8081;

    public static void main(String[] args) {
        try {
            ThreadPool threadPool = new ThreadPool();
            threadPool.start(4);
            final int memory = Integer.parseInt(args[0]);
            System.out.println("Memory: " + memory);

            Socket socket = new Socket(SERVER_MANAGER_ADDRESS, SERVER_MANAGER_PORT);
            TaggedConnection conn = new TaggedConnection(socket);

            WorkerHello workerHello = new WorkerHello(memory);
            conn.send(1,workerHello);

            while (true){
                TaggedConnection.Frame received = conn.receive();
                threadPool.execute(()->{
                    boolean success;
                    ExecuteRequest request = (ExecuteRequest) received.getMessage();

                    byte[] result = null;
                    try {
                        result = JobFunction.execute(request.getInput());
                        success = true;
                    } catch (JobFunctionException e) {
                        success = false;
                        result = e.getMessage().getBytes();
                    }

                    ExecuteResponse response = new ExecuteResponse(request.getAuthClientID(), success, request.getMemoryNeeded(),result);

                    try {
                        conn.send(received.getTag(),response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
