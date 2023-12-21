package com.faas.server_manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerManager {
    private static final int CLIENT_PORT = 8080;
    private  static final String WORKER_ADDRESS = "localhost";
    private static final int WORKER_PORT = 8081;


    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(CLIENT_PORT); // need to close this
            System.out.println("Server listening on port " + CLIENT_PORT + "...");

            while (true) {
                Socket socket = ss.accept(); // need to close this
                System.out.println("Accepted connection from client.");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String request = in.readLine();
                System.out.println("Client request: " + request);
                out.println("Message received!");
                System.out.println("Sent message to client.");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
