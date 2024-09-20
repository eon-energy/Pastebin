package ru.ion.app.services;

import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class TextFileService {
    public void writeStringToFile(String text, File file) {
        try (FileWriter fileWriter = new FileWriter(file);) {
            fileWriter.write(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readFileToString(File file) {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        if (!text.isEmpty()) {
            text.deleteCharAt(text.length() - 1); // Удаляем последний \n
        }
        return text.toString();
    }


}
