package ru.ion.app.services;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ion.app.exception.TextFileException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Service
public class TextFileService {
    /**
     * Записывает строковый текст в указанный файл.
     *
     * @param text текст для записи.
     * @param path путь к файлу, в который будет произведена запись.
     * @throws TextFileException если возникает ошибка при записи в файл.
     */
    public void writeStringToFile(String text, Path path) {
        try {
            Files.writeString(path, text, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new TextFileException("Error writing to the file: " + path, e);
        }
    }

    /**
     * Считывает содержимое файла в строку.
     *
     * @param path путь к файлу для чтения.
     * @return содержимое файла в виде строки.
     * @throws TextFileException если возникает ошибка при чтении файла.
     */
    public String readFileToString(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TextFileException("Error reading the file: " + path, e);
        }
    }
}