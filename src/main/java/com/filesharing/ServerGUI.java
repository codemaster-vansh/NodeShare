package com.filesharing;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private String adminPassword;

    public ServerGUI(String adminPassword) {
        this.adminPassword = adminPassword;
        setTitle("Server");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
        promptForPassword();
    }

    private void initComponents() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder("Connected Users"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logScrollPane, userScrollPane);
        splitPane.setResizeWeight(0.7);

        add(splitPane, BorderLayout.CENTER);
    }

    private void promptForPassword() {
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(this, passwordField, "Enter Admin Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            if (!new String(passwordField.getPassword()).equals(adminPassword)) {
                JOptionPane.showMessageDialog(this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } else {
            System.exit(0);
        }
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public void addUser(String username) {
        SwingUtilities.invokeLater(() -> userListModel.addElement(username));
    }

    public void removeUser(String username) {
        SwingUtilities.invokeLater(() -> userListModel.removeElement(username));
    }
}
