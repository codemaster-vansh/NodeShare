package com.filesharing;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileReceiver {
    private ClientGUI clientGUI;

    public FileReceiver(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
    }

    public void receiveFile(String sender, String fileName, byte[] fileData) {
        int option = JOptionPane.showConfirmDialog(clientGUI,
                sender + " wants to send you a file: " + fileName + ". Accept?",
                "Incoming File", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(fileName));
            int result = fileChooser.showSaveDialog(clientGUI);

            if (result == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    fos.write(fileData);
                    JOptionPane.showMessageDialog(clientGUI, "File saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(clientGUI, "Error saving file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
