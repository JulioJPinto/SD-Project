package com.faas.client;

import com.faas.client.Client;
import com.faas.common.*;
import com.faas.common.ExecuteResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

public class ClientInterface {
    public static void main(String[] args){
        try (Scanner inputScanner = new Scanner(System.in)) {
            ThreadPool threadPool = new ThreadPool();
            threadPool.start(4);
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

            AtomicInteger jobCounter = new AtomicInteger(0);
            while (true) {
                System.out.println("1 - Enviar Pedido\n2 - Consulta\n9 - Sair");
                int input = inputScanner.nextInt();
                if (input == 1) {
                    System.out.println("Nome do ficheiro e memória necessária em linhas separadas.");
                    String filename = inputScanner.next();
                    int memoryNeeded = inputScanner.nextInt();
                    jobCounter.increment();

                    String currFilename = filename;
                    int currMemoryNeeded = memoryNeeded;
                    threadPool.execute(()->{
                        int threadJobCounter = jobCounter.get();
                        String threadFilename = currFilename;
                        int threadMem = currMemoryNeeded;

                        System.out.println("A enviar job nº " + threadJobCounter);

                        boolean success = false;
                        try {
                            success = client.sendRequest(threadFilename,threadMem,threadJobCounter);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        if (success)
                            System.out.println("Job nº " + threadJobCounter + " com input no ficheiro: " + threadFilename + " executado com sucesso.");
                        else
                            System.out.println("Job nº " + threadJobCounter + " com input no ficheiro: " + threadFilename + " falhou");

                    });
                } else if (input == 2) {
                    threadPool.execute(()->{
                        try {
                            StatusResponse status = client.getStatus();
                            System.out.println("Atualmente existem " + status.getPendingTasks() + " tarefas pendentes e a memória disponível é " + status.getAvailableMemory() + ".");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                    });
                } else if (input == 9) {
                    client.closeClient();
                    threadPool.stop();
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
    }
}
