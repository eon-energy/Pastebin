package ru.ion.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;


import java.io.*;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class S3Service {

    private final String BUCKET_NAME = "bin-backed";
    private final S3TransferManager transferManager;
    private final S3AsyncClient s3AsyncClient;

    @Autowired
    public S3Service(S3TransferManager transferManager, S3AsyncClient s3AsyncClient) {
        this.transferManager = transferManager;
        this.s3AsyncClient = s3AsyncClient;
    }


    public void uploadFile(String key, File file) { // key = name of file in cloud
        UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                .putObjectRequest(req -> req.bucket(BUCKET_NAME).key(key))
                .addTransferListener(LoggingTransferListener.create())
                .source(Paths.get(file.getAbsolutePath()))
                .build();

        FileUpload upload = transferManager.uploadFile(uploadFileRequest);
        upload.completionFuture().join();
    }


    public void downloadFile(String key) { // key = name of file in cloud
        DownloadFileRequest downloadFileRequest =
                DownloadFileRequest.builder()
                        .getObjectRequest(req -> req.bucket(BUCKET_NAME).key(key))
                        .destination(Paths.get("downloaded.txt"))
                        .addTransferListener(LoggingTransferListener.create())
                        .build();

        FileDownload download = transferManager.downloadFile(downloadFileRequest);

        download.completionFuture().join();

    }

    public void deleteFile(String key){
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build();

        CompletableFuture<DeleteObjectResponse> future = s3AsyncClient.deleteObject(deleteObjectRequest);

        DeleteObjectResponse response = future.join();
    }
}

