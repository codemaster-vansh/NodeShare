package com.filesharing;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    public static List<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private UserManager userManager;
    private ServerGUI serverGUI;
    private FileManager fileManager;

    public ClientHandler(Socket socket, UserManager userManager, ServerGUI serverGUI, FileManager fileManager) {
        try {
            this.socket = socket;
            this.userManager = userManager;
            this.serverGUI = serverGUI;
            this.fileManager = fileManager;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientHandlers.add(this);
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        try {
            handleAuthentication();
            broadcastUserList();
            listenForMessages();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void handleAuthentication() throws IOException {
        while (socket.isConnected()) {
            String authRequest = bufferedReader.readLine();
            String[] parts = authRequest.split(",");
            String authType = parts[0];
            String username = parts[1];
            String password = parts[2];

            if ("REGISTER".equals(authType)) {
                if (userManager.register(username, password)) {
                    sendMessage("REGISTER_SUCCESS");
                    this.clientUsername = username;
                    serverGUI.log(username + " has registered and connected.");
                    serverGUI.addUser(username);
                    break;
                } else {
                    sendMessage("REGISTER_FAILURE");
                }
            } else if ("LOGIN".equals(authType)) {
                if (userManager.login(username, password)) {
                    sendMessage("LOGIN_SUCCESS");
                    this.clientUsername = username;
                    serverGUI.log(username + " has logged in.");
                    serverGUI.addUser(username);
                    break;
                } else {
                    sendMessage("LOGIN_FAILURE");
                }
            }
        }
    }

    private void listenForMessages() throws IOException {
        while (socket.isConnected()) {
            String messageFromClient = bufferedReader.readLine();
            if (messageFromClient == null) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
            String[] parts = messageFromClient.split(",");
            String messageType = parts[0];

            if ("FILE".equals(messageType)) {
                String recipient = parts[1];
                String fileName = parts[2];
                long fileSize = Long.parseLong(parts[3]);
                serverGUI.log("Receiving " + fileName + " from " + clientUsername + " for " + recipient);

                byte[] fileData = new byte[(int) fileSize];
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                dataInputStream.readFully(fileData, 0, fileData.length);

                try {
                    fileManager.saveFile(fileName, fileData);
                    serverGUI.log("Saved " + fileName + " to server.");
                } catch (IOException e) {
                    serverGUI.log("Error saving " + fileName + " to server.");
                    e.printStackTrace();
                }

                for (ClientHandler clientHandler : clientHandlers) {
                    if (clientHandler.clientUsername.equals(recipient)) {
                        clientHandler.sendFile(clientUsername, fileName, fileData);
                        break;
                    }
                }
            }
        }
    }

    public void sendFile(String sender, String fileName, byte[] fileData) throws IOException {
        sendMessage("INCOMING_FILE," + sender + "," + fileName + "," + fileData.length);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.write(fileData);
        dataOutputStream.flush();
        serverGUI.log("Sent " + fileName + " from " + sender + " to " + clientUsername);
    }

    private void broadcastUserList() {
        StringBuilder userList = new StringBuilder("USERLIST");
        for (ClientHandler clientHandler : clientHandlers) {
            userList.append(",").append(clientHandler.clientUsername);
        }
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.sendMessage(userList.toString());
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void sendMessage(String message) throws IOException {
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        if (clientUsername != null) {
            serverGUI.removeUser(clientUsername);
            broadcastUserList();
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
