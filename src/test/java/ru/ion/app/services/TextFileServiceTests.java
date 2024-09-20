package ru.ion.app.services;

import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class TextFileServiceTests {
    @Autowired
    private TextFileService textFileService;

    @Test
    void writeStringToFileTest() throws IOException {
        String text = "test text";
        File tempFile = new File("tempFile.txt");
        tempFile.createNewFile();
        textFileService.writeStringToFile(text, tempFile);
    }

    @Test
    void readStringFromFile() {
        File file = new File("C:\\Users\\wwwio\\Desktop\\app\\tempFile.txt");
        String text = textFileService.readFileToString(file);
        assertEquals (text,"test text");
    }
}
