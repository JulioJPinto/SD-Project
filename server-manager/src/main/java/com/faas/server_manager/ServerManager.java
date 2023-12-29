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

    private static AtomicInteger currentAuthUsers = new AtomicInteger(0);
    private static final Queue<TaggedConnection.Frame> messagesSent = new ConcurrentLinkedQueue<TaggedConnection.Frame>();

    private static final Queue<TaggedConnection.Frame> messagesReceived = new ConcurrentLinkedQueue<TaggedConnection.Frame>();
    private static final SynchronizedMap<String,User> users = new SynchronizedMap<>();

    private static final ReentrantLock authenticationLock = new ReentrantLock();

    private static int authenticateUser(AuthenticationRequest authReq){
        User user = null;
        if (authReq.getType() == 2) {
            if (!users.containsKey(authReq.getUsername())) {
                user = new User(authReq.getUsername(), authReq.getPassword());
                users.put(user.getUsername(), user);
                System.out.println("User novo:\n" + user.toString());
                currentAuthUsers.increment();
                return currentAuthUsers.get();
            }
        } else if (authReq.getType() == 1) {
            if (users.containsKey(authReq.getUsername()))
                if (Objects.equals(users.get(authReq.getUsername()).getPassword(), authReq.getPassword())) {
                    user = users.get(authReq.getUsername());
                    System.out.println("User existente:\n" + user.toString());
                    currentAuthUsers.increment();
                    return currentAuthUsers.get();
                }
        }
        System.out.println(currentAuthUsers.get());
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

                            int finalAuthUserId = authUserId;
                            new Thread(() -> {
                                //recebe dos workers e envia para os clientes
                               while (true){
                                  TaggedConnection.Frame response = messagesReceived.peek();
                                   if (response != null && response.getMessage().getAuthClientID() == finalAuthUserId){
                                      messagesReceived.remove(response);

                                      try {
                                          conn.send(response);
                                      } catch (IOException e) {
                                          throw new RuntimeException(e);
                                      }
                                  }
                               }
                            }).start();

                            while (true) {
                                //recebe dos clientes e envia para os workers
                               TaggedConnection.Frame request = conn.receive();


                               messagesSent.add(request);
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
                    Socket socket = serverWorkerSocket.accept();
                    System.out.println("Server worker connected.");
                    new Thread(() ->{
                        try (TaggedConnection conn = new TaggedConnection(socket)){

                            new Thread(() ->{
                                //envia para os workers
                                while (true) {
                                    TaggedConnection.Frame request = messagesSent.poll();

                                    if (request != null) {
                                        try {
                                            conn.send(request);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }).start();

                            while (true) {
                                //recebe dos workers
                                TaggedConnection.Frame response = conn.receive();
                                messagesReceived.add(response);
                            }
                        } catch (IOException | InvocationTargetException | InstantiationException |
                                 ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();

                }

            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        listenClientThread.start();
        serverWorkerThread.start();
    }
}
