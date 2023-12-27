package com.faas.client;

import com.faas.client.Client;
import com.faas.common.TestMessage;
import com.faas.common.User;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

public class ClientInterface {
    public static void main(String[] args){
        try (Scanner inputScanner = new Scanner(System.in)) {
            Client client = new Client();
            int authClientID;

            while (true){
                System.out.println("1 - Login\n2 - Registar");
                int input = inputScanner.nextInt();
                if (input == 1) {
                    System.out.println("Escreva o nome de utilizador e a password em linhas separadas");
                    String username = inputScanner.next();
                    String password = inputScanner.next();

                    authClientID = client.loginUser(username,password);
                    if (authClientID == 0)
                        System.out.println("Credenciais inválidas");
                    else {
                        System.out.println("Utilizador: " + username + " deu login.");
                        break;
                    }
                } else if (input == 2){
                    System.out.println("Escreva o nome de utilizador e a password em linhas separadas");
                    String username = inputScanner.next();
                    String password = inputScanner.next();

                    authClientID = client.registerNewUser(username,password);
                    if (authClientID == 0)
                        System.out.println("Credenciais inválidas");
                    else {
                        System.out.println("Utilizador: " + username + " registado.");
                        break;
                    }
                }
            }

            while (true) {
                System.out.println("1 - Enviar Pedido\n9 - Sair");
                System.out.flush();
                int input = inputScanner.nextInt();
                if (input == 1) {
                    System.out.println("Enviar mensagens de teste");

                    for(int i = 0; i < 10; i++) {
                        int threadN = i;
                        new Thread(() -> {
                            try {
                                client.sendMessage("Enviada da thread " + threadN);
                                TestMessage received = (TestMessage) client.receiveMessage();
                                System.out.println("Thread " + threadN + " recebeu: " + received.getS());

                            } catch (IOException | ClassNotFoundException | InvocationTargetException |
                                     NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    }
                } else if (input == 9) {
                    client.closeClient();
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
    }
}
