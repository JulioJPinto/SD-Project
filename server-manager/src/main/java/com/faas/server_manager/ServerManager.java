package com.faas.server_manager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public class ServerManager {
    private static final int CLIENT_PORT = 8080;
    private static final int SERVER_WORKER_PORT = 8081;
    private static final Queue<String> messages = new ConcurrentLinkedQueue<String>();

    public static void main(String[] args) {
        Thread clientThread = new Thread(() -> {
            try (ServerSocket clientSocket = new ServerSocket(CLIENT_PORT)) {
                System.out.println("Waiting for clients on port " + CLIENT_PORT + "...");

                while (true) {
                    Socket socket = clientSocket.accept(); // need to close this
                    System.out.println("Client connected.");

                    DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                    String message = in.readUTF();
                    System.out.println("Received message from client: " + message);

                    out.writeUTF("Hello from manager!");
                    out.flush();

                    messages.add(message);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    String response = messages.poll();
                    if (response == null) {
                        response = "No response";
                    }
                    out.writeUTF(response);
                    out.flush();

                    in.close();
                    out.close();
                    socket.close();
                }

            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        Thread serverWorkerThread = new Thread(() -> {
            try (ServerSocket serverWorkerSocket = new ServerSocket(SERVER_WORKER_PORT)) {
                System.out.println("Waiting for server workers on port " + SERVER_WORKER_PORT + "...");

                while (true) {
                    Socket socket = serverWorkerSocket.accept(); // need to close this
                    System.out.println("Server worker connected.");

                    DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                    String message = in.readUTF();
                    System.out.println("Received message from server worker: " + message);

                    out.writeUTF("Hello from manager!");
                    out.flush();

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    String queueMessage = messages.poll();
                    if (queueMessage == null) {
                        queueMessage = "No message";
                    }
                    out.writeUTF(queueMessage);
                    out.flush();
                    String response = in.readUTF();
                    messages.add(response);

                    in.close();
                    out.close();
                    socket.close();
                }

            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        clientThread.start();
        serverWorkerThread.start();
    }
}
