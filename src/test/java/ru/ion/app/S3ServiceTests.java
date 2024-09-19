package ru.ion.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ion.app.services.S3Service;

import java.io.File;
import java.io.IOException;

@SpringBootTest
@ActiveProfiles("test")
public class S3ServiceTests {
    @Autowired
    private S3Service s3Service;

    @Test
    void UploadFileTest() throws IOException {
        File file = new File("test.txt");
        file.createNewFile();
        s3Service.uploadFile("key", file);
        file.delete();
    }

    @Test
    void DownloadFileTest() {
        s3Service.downloadFile("key");
    }

    @Test
    void DeleteFileTest(){
        s3Service.deleteFile("key");
    }
}
