package ru.ion.app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ion.app.DTO.PasteData;
import ru.ion.app.entitys.Paste;
import ru.ion.app.repositories.PasteRepository;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PasteService {

    private final S3Service s3Service;
    private final TextFileService textFileService;
    private final KeyGenerationService keyGenerationService;
    private final PasteRepository pasteRepository;

    public String uploadToCloud(PasteData pasteData) throws NoSuchAlgorithmException, IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String generatedKey = keyGenerationService.generateKey();


        Paste paste = new Paste();
        paste.setCreateDate(LocalDate.now());
        paste.setEndDate(pasteData.getEndDate());
        paste.setKey(generatedKey);


        File tempFile = File.createTempFile(generatedKey, ".tmp", new File(tempDir));

        textFileService.writeStringToFile(pasteData.getText(), tempFile);
        s3Service.uploadFile(tempFile, generatedKey);
        pasteRepository.save(paste);

        tempFile.delete();

        return generatedKey;
    }

    public PasteData downloadFromCloud(String key) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        Paste paste = pasteRepository.findByKey(key).get(); //TODO выбросить исключение

        File tempFile = File.createTempFile(key, ".tmp", new File(tempDir));
        s3Service.downloadFile(tempFile.toPath(), key);

        PasteData pasteData = new PasteData();
        pasteData.setEndDate(paste.getEndDate());
        pasteData.setText(textFileService.readFileToString(tempFile));

        tempFile.delete();

        return pasteData;
    }

}
