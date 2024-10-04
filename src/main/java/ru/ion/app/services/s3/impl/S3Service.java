package ru.ion.app.services.s3.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ion.app.exception.S3ServiceException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;


import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class S3Service {

    private final String bucketName = "bin-backed";
    private final S3TransferManager transferManager;
    private final S3AsyncClient s3AsyncClient;

    /**
     *
     * Загружает файл в хранилище S3.
     *
     * <p>Метод выполняет асинхронную загрузку указанного файла в S3 с использованием заданного ключа.
     * После успешной загрузки, если это необходимо, можно выполнить дополнительные действия через {@code CompletableFuture}.
     *
     * @param path файл для загрузки.
     * @param key  уникальный ключ (имя файла) в облачном хранилище S3.
     * @return {@code CompletableFuture<CompletedFileUpload>} представляющий собой будущий результат завершения операции загрузки.
     * @throws IllegalArgumentException если {@code file} или {@code key} равны {@code null} или {@code key} пустой.
     * @throws S3ServiceException если загрузка файла в S3 завершается с ошибкой.
     */
    public CompletableFuture<CompletedFileUpload> uploadFile(Path path, String key) {
        if (path == null || key == null || key.isEmpty()) {
            throw new IllegalArgumentException("path and key must not be null or empty");
        }

        UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                .putObjectRequest(req -> req.bucket(bucketName).key(key))
                .source(path)
                .build();

        FileUpload upload = transferManager.uploadFile(uploadFileRequest);

        return upload.completionFuture()
                .exceptionally(ex -> {
                    throw new S3ServiceException("failed to upload file to S3", ex);
                });
    }

    /**
     * Скачивает файл из хранилища S3 и сохраняет его по указанному пути.
     *
     * <p>Метод выполняет асинхронную загрузку файла из S3 с использованием заданного ключа и сохраняет
     * его по указанному пути {@code path}.
     *
     * @param path путь на локальной файловой системе, куда будет сохранён скачанный файл.
     * @param key  уникальный ключ (имя файла) в облачном хранилище S3.
     * @return {@code CompletableFuture<Void>} представляющий собой будущий результат завершения операции скачивания.
     * @throws IllegalArgumentException если {@code path} или {@code key} равны {@code null} или {@code key} пустой.
     * @throws S3ServiceException если скачивание файла из S3 завершается с ошибкой.
     */
    public CompletableFuture<CompletedFileDownload> downloadFile(Path path, String key) {
        if (path == null || key == null || key.isEmpty()) {
            throw new IllegalArgumentException("path and key must not be null or empty");
        }

        DownloadFileRequest downloadFileRequest = DownloadFileRequest.builder()
                .getObjectRequest(req -> req.bucket(bucketName).key(key))
                .destination(path)
                .build();

        FileDownload download = transferManager.downloadFile(downloadFileRequest);

        return download.completionFuture()
                .exceptionally(ex -> {
                    throw new S3ServiceException("failed to download file from S3", ex);
                });
    }

    /**
     * Удаляет файл из хранилища S3 асинхронно.
     *
     * @param key уникальный ключ (имя файла) в облачном хранилище S3.
     * @return {@code CompletableFuture<Void>} представляющий собой будущий результат завершения операции удаления.
     * @throws IllegalArgumentException если {@code key} равен {@code null} или пустой строке.
     * @throws S3ServiceException       если удаление файла в S3 завершается с ошибкой.
     */
    public CompletableFuture<DeleteObjectResponse> deleteFile(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Ключ не должен быть null или пустым.");
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3AsyncClient.deleteObject(deleteObjectRequest)
                .exceptionally(ex -> {
                    throw new S3ServiceException("Не удалось удалить файл из S3", ex);
                });
    }

}

