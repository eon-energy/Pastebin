package ru.ion.app.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
@ActiveProfiles("test")
public class S3ServiceTests {
    @Autowired
    private S3Service s3Service;

    @Test
    void uploadFileTest() throws IOException {
        File file = new File("test.txt");
        file.createNewFile();
        s3Service.uploadFile(file,"key");
        file.delete();
    }

    @Test
    void downloadFileTest() {
        s3Service.downloadFile(Path.of("C:\\Users\\wwwio\\Desktop\\app\\src\\test\\java\\ru\\ion\\app\\tempFiles\\forDownload"),"generatedKey");
    }

    @Test
    void deleteFileTest(){
        s3Service.deleteFile("key");
    }
}
