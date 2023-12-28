package com.faas.client;

import com.faas.client.Client;
import com.faas.common.ExecuteResponse;
import com.faas.common.ExecuteResponse;
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
                        System.out.println("Utilizador: " + username + " deu login com o ID " + authClientID);
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
                        System.out.println("Utilizador: " + username + " registado com o ID " + authClientID);
                        break;
                    }
                }
            }

            int jobCounter = 0;
            while (true) {
                System.out.println("1 - Enviar Pedido\n2 - Consulta\n9 - Sair");
                int input = inputScanner.nextInt();
                if (input == 1) {
                    System.out.println("Nome do ficheiro e memória necessária em linhas separadas.");
                    String filename = inputScanner.next();
                    int memoryNeeded = inputScanner.nextInt();
                    jobCounter += 1;

                    String currFilename = filename;
                    int currMemoryNeeded = memoryNeeded;
                    int currJobCounter = jobCounter;
                    new Thread(() -> {
                        String threadFilename = currFilename;
                        int threadMem = currMemoryNeeded;
                        int threadJobCounter = currJobCounter;

                        System.out.println("A enviar job nº " + threadJobCounter);

                        try {
                            client.sendRequest(threadFilename,threadMem);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        System.out.println("Job nº " + threadJobCounter + " com input no ficheiro: " + threadFilename);

                    }).start();

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
