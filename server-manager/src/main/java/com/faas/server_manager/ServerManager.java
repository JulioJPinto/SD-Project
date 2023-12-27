package com.faas.server_manager;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import com.faas.common.*;

public class ServerManager {
    private static final int CLIENT_PORT = 8080;
    private static final int SERVER_WORKER_PORT = 8081;

    private static int currentAuthUsers = 0;
    private static final Queue<Message> messages = new ConcurrentLinkedQueue<Message>();
    private static final Map<String,User> users = new HashMap<>();

    private static final ReentrantLock authenticationLock = new ReentrantLock();

    private static int authenticateUser(AuthenticationRequest authReq){
        User user = null;
        try {
            authenticationLock.lock();
            if (authReq.getType() == 2) {
                if (!users.containsKey(authReq.getUsername())) {
                    user = new User(authReq.getUsername(), authReq.getPassword());
                    users.put(user.getUsername(), user);
                    System.out.println("User novo:\n" + user.toString());
                    currentAuthUsers += 1;
                    return currentAuthUsers;
                }
            } else if (authReq.getType() == 1) {
                if (users.containsKey(authReq.getUsername()))
                    if (Objects.equals(users.get(authReq.getUsername()).getPassword(), authReq.getPassword())) {
                        user = users.get(authReq.getUsername());
                        System.out.println("User existente:\n" + user.toString());
                        currentAuthUsers += 1;
                        return currentAuthUsers;
                    }
            }
        }finally {
            authenticationLock.unlock();
        }
        System.out.println(currentAuthUsers);
        return 0;
    }

    public static void main(String[] args) {
        Thread listenClientThread = new Thread(() -> {
            try (ServerSocket clientSocket = new ServerSocket(CLIENT_PORT)) {
                System.out.println("Waiting for clients on port " + CLIENT_PORT + "...");

                while (true) {
                    Socket socket = clientSocket.accept();
                    new Thread(() ->{
                        try (TaggedConnection conn = new TaggedConnection(socket)){
                            System.out.println("Client connected.");
                            boolean notAuthenticated = true;

                            int authUserId = 0;
                            while (notAuthenticated) {
                                TaggedConnection.Frame authFrame = conn.receive();
                                AuthenticationRequest authReq = (AuthenticationRequest) authFrame.getMessage();

                                System.out.println(authReq.toString());

                                authUserId = authenticateUser(authReq);

                                AuthenticationResponse authResp = new AuthenticationResponse(authUserId);
                                System.out.println(authResp.toString());
                                conn.send(authFrame.getTag(), authResp);

                                if (authUserId != 0)
                                    notAuthenticated = false;

                                for (Map.Entry<String, User> entry : users.entrySet()) {
                                    System.out.println("Key: " + entry.getKey() + ":" + "Valor: " + entry.getValue().toString());
                                }
                            }

                            while (true) {
                                    TaggedConnection.Frame receivedFrame = conn.receive();

                                    TestMessage testReceive = (TestMessage) receivedFrame.getMessage();

                                    //sleep relacionado ao bug que descrevi no demultiplexer, assim funcionou sempre
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                conn.send(receivedFrame.getTag(),testReceive);
                            }

                        } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                                 InstantiationException | IllegalAccessException e){
                            System.out.println("Error: " + e.getMessage());

                        }
                    }).start();
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
                    TaggedConnection conn = new TaggedConnection(socket);

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    Message queueMessage = messages.poll();
                }

            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        listenClientThread.start();
        //serverWorkerThread.start();
    }
}
