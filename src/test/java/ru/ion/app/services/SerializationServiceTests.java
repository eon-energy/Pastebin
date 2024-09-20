package ru.ion.app.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ion.app.DTO.PasteData;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("test")
public class SerializationServiceTests {
    @Autowired
    private SerializationService serializationService;

    @Test
    void serializeTest() throws IOException {
        PasteData pasteData = new PasteData();
        pasteData.setText("text");
        pasteData.setCreateDate(LocalDate.of(2005, 9, 27));
        pasteData.setEndDate(LocalDate.of(2025, 9, 27));
        System.out.println(pasteData);

        File file = new File("C:\\Users\\wwwio\\Desktop\\app\\src\\test\\java\\ru\\ion\\app\\tempFiles\\forSerialization");
        file.createNewFile();
        serializationService.serialize(pasteData, file);
    }

    @Test
    void deserializeTest() {
        File file = new File("C:\\Users\\wwwio\\Desktop\\app\\src\\test\\java\\ru\\ion\\app\\tempFiles\\forSerialization");
        PasteData pasteData = serializationService.deserialize(file);
        System.out.println(pasteData);
    }
}
