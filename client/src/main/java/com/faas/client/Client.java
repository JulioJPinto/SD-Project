package com.faas.client;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_MANAGER_ADDRESS = "localhost"; // 127.0.0.1
    private static final int SERVER_MANAGER_PORT = 8080;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_MANAGER_ADDRESS, SERVER_MANAGER_PORT);
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            out.writeUTF("Hello from client!");
            out.flush();
            System.out.println("Sent message to manager.");

            String response = in.readUTF();
            System.out.println("Server response: " + response);
            response = in.readUTF();
            System.out.println("Server response: " + response);

            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}