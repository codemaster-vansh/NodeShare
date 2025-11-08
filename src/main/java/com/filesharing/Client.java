package com.filesharing;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private ClientGUI clientGUI;

    public Client(String host, int port) {
        try {
            this.socket = new Socket(host, port);
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendFile(String recipient, String fileName, byte[] fileData) {
        try {
            sendMessage("FILE," + recipient + "," + fileName + "," + fileData.length);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write(fileData);
            dataOutputStream.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String msgFromServer;
            while (socket.isConnected()) {
                try {
                    msgFromServer = bufferedReader.readLine();
                    if (msgFromServer == null) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break;
                    }

                    if ("LOGIN_SUCCESS".equals(msgFromServer) || "REGISTER_SUCCESS".equals(msgFromServer)) {
                        clientGUI.showMainApp();
                    } else if ("LOGIN_FAILURE".equals(msgFromServer) || "REGISTER_FAILURE".equals(msgFromServer)) {
                        clientGUI.showLoginError("Invalid credentials or username taken.");
                    } else if (msgFromServer.startsWith("USERLIST")) {
                        String[] users = msgFromServer.substring(9).split(",");
                        clientGUI.updateUsers(users);
                    } else if (msgFromServer.startsWith("INCOMING_FILE")) {
                        String[] parts = msgFromServer.split(",");
                        String sender = parts[1];
                        String fileName = parts[2];
                        long fileSize = Long.parseLong(parts[3]);

                        byte[] fileData = new byte[(int) fileSize];
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        dataInputStream.readFully(fileData, 0, fileData.length);

                        clientGUI.receiveFile(sender, fileName, fileData);
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    public void setGui(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
    }

    public void sendMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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

    public static void main(String[] args) {
        // The client will be started from the GUI
    }
}
