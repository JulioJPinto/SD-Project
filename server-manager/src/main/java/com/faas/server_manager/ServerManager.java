package com.faas.server_manager;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import com.faas.common.*;

public class ServerManager {
    private static final int CLIENT_PORT = 8080;
    private static final int SERVER_WORKER_PORT = 8081;
    private static final BoundedBuffer<Tuple<WorkerStats, TaggedConnection.Frame>> messagesSent = new BoundedBuffer<>(50);
    private static final BoundedBuffer<TaggedConnection.Frame> messagesReceived = new BoundedBuffer<>(50);


    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool();
        threadPool.start(16);

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

                                authUserId = Authenticator.authenticateUser(authReq);

                                AuthenticationResponse authResp = new AuthenticationResponse(authUserId);
                                System.out.println(authResp.toString());
                                conn.send(authFrame.getTag(), authResp);

                                if (authUserId != 0)
                                    notAuthenticated = false;

                                for (Map.Entry<String, User> entry : Authenticator.getUsersEntries()) {
                                    System.out.println("Key: " + entry.getKey() + ":" + "Valor: " + entry.getValue().toString());
                                }
                            }

                            int finalAuthUserId = authUserId;
                            threadPool.execute(() -> {
                                //recebe dos workers e envia para os clientes
                                while (true){
                                    TaggedConnection.Frame response = messagesReceived.peek();
                                    if (response != null && response.getMessage().getAuthClientID() == finalAuthUserId) {
                                        messagesReceived.remove(response);

                                        try {
                                            conn.send(response);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            });

                            while (true) {
                                //recebe dos clientes e envia para a thread do manager responsável pelos workers
                               TaggedConnection.Frame request = conn.receive();

                                if (request.getMessage().getClass().getName().equals(StatusRequest.class.getName())){
                                    threadPool.execute(() -> {
                                        StatusResponse statusResp = new StatusResponse(finalAuthUserId,messagesSent.length(), WorkersInfo.getAvailableMemory());
                                        try {
                                            conn.send(request.getTag(),statusResp);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                } else {
                                    WorkerStats worker = null;
                                    while (worker == null) {
                                        worker = Scheduler.chooseNextWorker(request.getMessage());
                                    }
                                    messagesSent.produce(new Tuple<>(worker, request));
                                    System.out.println("Pendente: " + messagesSent.length());
                                }
                            }

                        } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                                 InstantiationException | IllegalAccessException e){
                            System.out.println("Error: " + e.getMessage());

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }

            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
// =====================================================================================================================
        Thread serverWorkerThread = new Thread(() -> {
            try (ServerSocket serverWorkerSocket = new ServerSocket(SERVER_WORKER_PORT)) {
                System.out.println("Waiting for server workers on port " + SERVER_WORKER_PORT + "...");

                while (true) {
                    Socket socket = serverWorkerSocket.accept();
                    System.out.println("Server worker connected.");
                    new Thread(() ->{
                        WorkerHello workerHello = null;
                        WorkerStats workerStats = null;
                        try (TaggedConnection conn = new TaggedConnection(socket)){
                            workerHello = (WorkerHello) conn.receive().getMessage();
                            int workerId = WorkersInfo.generateId();
                            workerStats = new WorkerStats(workerId, workerHello.getTotalMemory(), conn);
                            WorkersInfo.updateWorkerMemory(workerId, workerHello.getTotalMemory(), false);
                            WorkersInfo.addWorker(workerId, workerStats);
                            // print every worker inside the map
                            for (Map.Entry<Integer, WorkerStats> entry : WorkersInfo.getWorkersEntries()) {
                                System.out.println("Key: " + entry.getKey() + ":" + "Valor: " + entry.getValue().toString());
                            }
                            System.out.println("Memory: " + workerHello.getTotalMemory());
                            System.out.println("Total memory: " + WorkersInfo.getAvailableMemory());

                            threadPool.execute(() -> {
                                //envia requests para os workers
                                while (true) {
                                    Tuple<WorkerStats, TaggedConnection.Frame> request = messagesSent.peek();
                                    if (request != null && request.getFirst().getId() == workerId) {
                                        messagesSent.remove(request);

                                        try {
                                            conn.send(request.getSecond());
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            });

                            while (true) {
                                //recebe respostas a requests dos workers
                                TaggedConnection.Frame response = conn.receive();
                                WorkersInfo.updateWorkerMemory(workerId, ((ExecuteResponse)response.getMessage()).getMemoryUsed(), false);
                                messagesReceived.produce(response);
                            }
                        } catch (IOException | InvocationTargetException | InstantiationException |
                                 ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                                 InterruptedException e) {
                            throw new RuntimeException(e);
                        } finally {
                            assert workerHello != null;
                            assert workerStats != null;
                            WorkersInfo.updateWorkerMemory(workerStats.getId(), workerHello.getTotalMemory(), true);
                            WorkersInfo.removeWorker(workerStats.getId());
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
