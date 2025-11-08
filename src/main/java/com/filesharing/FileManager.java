package com.filesharing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {
    private final String uploadDir = "server_files/";

    public FileManager() {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String saveFile(String fileName, byte[] fileData) throws IOException {
        File file = new File(uploadDir + fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileData);
        }
        return file.getAbsolutePath();
    }
}
