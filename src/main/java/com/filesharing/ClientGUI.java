package com.filesharing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ClientGUI extends JFrame {
    private Client client;
    private String username;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    public ClientGUI() {
        showLoginScreen();
    }

    private void showLoginScreen() {
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField serverIpField = new JTextField("127.0.0.1", 20);

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Server IP:"));
        panel.add(serverIpField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        panel.add(loginButton);
        panel.add(registerButton);

        loginButton.addActionListener((ActionEvent e) -> {
            this.username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String serverIp = serverIpField.getText();
            client = new Client(serverIp, 1234);
            client.setGui(this);
            client.sendMessage("LOGIN," + username + "," + password);
            client.listenForMessage();
        });

        registerButton.addActionListener((ActionEvent e) -> {
            this.username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String serverIp = serverIpField.getText();
            client = new Client(serverIp, 1234);
            client.setGui(this);
            client.sendMessage("REGISTER," + username + "," + password);
            client.listenForMessage();
        });

        setContentPane(panel);
        setTitle("Login");
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void showMainApp() {
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder("Online Users"));

        JButton sendFileButton = new JButton("Send File");
        sendFileButton.addActionListener((ActionEvent e) -> {
            String selectedUser = userList.getSelectedValue();
            if (selectedUser != null) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        byte[] fileData = new byte[(int) selectedFile.length()];
                        FileInputStream fileInputStream = new FileInputStream(selectedFile);
                        fileInputStream.read(fileData);
                        fileInputStream.close();
                        client.sendFile(selectedUser, selectedFile.getName(), fileData);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to send a file to.", "No User Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(userScrollPane, BorderLayout.CENTER);
        mainPanel.add(sendFileButton, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setTitle("File Sharer - " + username);
        pack();
        revalidate();
    }

    public void showLoginError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    public void updateUsers(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.equals(this.username)) {
                    userListModel.addElement(user);
                }
            }
        });
    }

    public void receiveFile(String sender, String fileName, byte[] fileData) {
        FileReceiver fileReceiver = new FileReceiver(this);
        fileReceiver.receiveFile(sender, fileName, fileData);
    }

    public static void main(String[] args) {
        new ClientGUI();
    }
}
