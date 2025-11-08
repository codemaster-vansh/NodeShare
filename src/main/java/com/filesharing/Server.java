package com.filesharing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.*;

/**
 * The Server class is the main entry point for the file sharing server.
 * It is responsible for creating a server socket, listening for incoming client connections,
 * and creating a new ClientHandler for each connected client.
 */
public class Server {

    private ServerSocket serverSocket;
    private UserManager userManager;
    private ServerGUI serverGUI;
    private FileManager fileManager;

    /**
     * Constructs a new Server instance.
     *
     * @param port          The port number to listen on.
     * @param adminPassword The admin password for the server GUI.
     */
    public Server(int port, String adminPassword) {
        try {
            serverSocket = new ServerSocket(port);
            userManager = new UserManager();
            serverGUI = new ServerGUI(adminPassword);
            fileManager = new FileManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the server.
     * This method creates a new thread that listens for incoming client connections.
     * For each new connection, it creates a new ClientHandler and starts it on a new thread.
     */
    public void start() {
        serverGUI.log("Server started...");
        new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    serverGUI.log("A new client has connected!");
                    ClientHandler clientHandler = new ClientHandler(socket, userManager, serverGUI, fileManager);
                    Thread thread = new Thread(clientHandler);
                    thread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * The main method of the server application.
     * It loads the admin password from the config.properties file, creates a new Server instance, and starts it.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        Config config = new Config("config/config.properties");
        String adminPassword = config.getProperty("admin.password");
        if (adminPassword == null || adminPassword.isEmpty()) {
            System.out.println("Admin password not found in config.properties. Using default password 'admin'.");
            adminPassword = "admin";
        }
        Server server = new Server(1234, adminPassword);
        server.start();
    }
}
