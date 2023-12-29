package com.faas.server_worker;

import com.faas.common.ExecuteRequest;
import com.faas.common.ExecuteResponse;
import com.faas.common.TaggedConnection;
import sd23.*;

import java.io.*;
import java.net.Socket;

public class ServerWorker {
    private static final String SERVER_MANAGER_ADDRESS = "localhost";
    private static final int SERVER_MANAGER_PORT = 8081;

    public static void main(String[] args) {
        try {
            final int memory = Integer.parseInt(args[0]);
            int usedMemory = 0;
            System.out.println("Memory: " + memory);

            Socket socket = new Socket(SERVER_MANAGER_ADDRESS, SERVER_MANAGER_PORT);
            TaggedConnection conn = new TaggedConnection(socket);
            while (true){
                TaggedConnection.Frame received = conn.receive();
                new Thread(()-> {
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

                    ExecuteResponse response = new ExecuteResponse(request.getAuthClientID(), success, result);

                    try {
                        conn.send(received.getTag(),response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
