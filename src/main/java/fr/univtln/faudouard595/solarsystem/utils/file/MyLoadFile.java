package fr.univtln.faudouard595.solarsystem.utils.file;

import java.io.*;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyLoadFile {

    // Method to load InputStream and write it to a temporary File
    private Optional<File> loadInstanceFile(String filePath) {
        InputStream inputStream = null;
        File tempFile = null;
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            if (inputStream == null) {
                return Optional.empty();
            }

            // Create a temporary file
            tempFile = File.createTempFile("loaded_", ".tmp");
            tempFile.deleteOnExit(); // Automatically delete the temp file when the JVM exits

            // Copy the InputStream to the temp file
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }

        } catch (IOException e) {
            log.error("Error writing InputStream to file", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing InputStream", e);
            }
        }
        return Optional.ofNullable(tempFile);
    }

    public boolean doesFileExist(String filePath) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        return inputStream != null; // If inputStream is not null, the file exists
    }

    public static Optional<File> loadFile(String filePath) {
        return new MyLoadFile().loadInstanceFile(filePath);
    }

    public static boolean fileExists(String filePath) {
        return new MyLoadFile().doesFileExist(filePath);
    }
}
