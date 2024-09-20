package ru.ion.app.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ion.app.DTO.PasteData;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("test")
public class PasteServiceTests {
    @Autowired
    private PasteService pasteService;

    @Test
    public void uploadToCloudTest() throws NoSuchAlgorithmException, IOException {
        PasteData pasteData = new PasteData();
        pasteData.setText("text");
        pasteData.setEndDate(LocalDate.of(2025, 9, 27));
        String key = pasteService.uploadToCloud(pasteData);
        System.out.println(key);
    }

    @Test
    public void downloadFromCloudTest() throws IOException {
        PasteData pasteData = pasteService.downloadFromCloud("Ds66SaQr20240920203406");
        System.out.println(pasteData);
    }

}
