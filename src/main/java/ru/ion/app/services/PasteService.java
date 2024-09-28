package ru.ion.app.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ion.app.DTO.KeyData;
import ru.ion.app.DTO.PasteData;
import ru.ion.app.entitys.Paste;
import ru.ion.app.exception.PasteServiceException;
import ru.ion.app.exception.S3ServiceException;
import ru.ion.app.repositories.PasteRepository;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PasteService {

    private final S3Service s3Service;
    private final TextFileService textFileService;
    private final KeyGenerationService keyGenerationService;
    private final PasteRepository pasteRepository;
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    /**
     * Сохраняет данные {@code PasteData} в файл, загружает этот файл в хранилище S3
     * и сохраняет объект {@code Paste} в репозиторий. При сохранении объект {@code Paste}
     * содержит ключ (ссылку) на {@code PasteData}, хранящиеся в S3.
     *
     * <p>Процесс выполнения метода включает следующие шаги:
     * <ol>
     *     <li>Генерация уникального ключа для сохранения Paste.</li>
     *     <li>Создание временного файла в системной временной директории.</li>
     *     <li>Запись текста из {@code pasteData} в временный файл.</li>
     *     <li>Асинхронная загрузка временного файла в S3 с использованием {@code s3Service}.</li>
     *     <li>После успешной загрузки в S3:
     *         <ul>
     *             <li>Сохранение объекта {@code Paste} в {@code pasteRepository}, содержащего ключ на {@code PasteData} в S3.</li>
     *             <li>Удаление временного файла из файловой системы.</li>
     *         </ul>
     *     </li>
     * </ol>
     *
     * @param pasteData данные для сохранения, содержащие текст и дату окончания.
     * @return {@code KeyData} содержащий сгенерированный уникальный ключ для сохраненного Paste.
     * @throws NoSuchAlgorithmException если возникает ошибка при генерации ключа.
     * @throws IOException              если происходит ошибка при создании или записи во временный файл.
     * @throws S3ServiceException если удаление файла из S3 завершается с ошибкой.
     */
    @Transactional
    public KeyData save(PasteData pasteData) throws NoSuchAlgorithmException, IOException, S3ServiceException {
        String generatedKey = keyGenerationService.generateKey();
        Path tempFilePath = Files.createTempFile(generatedKey, ".tmp");

        try {
            Paste paste = mapToPaste(pasteData, generatedKey);
            textFileService.writeStringToFile(pasteData.getText(), tempFilePath);

            s3Service.uploadFile(tempFilePath, generatedKey)
                    .thenRun(() -> {
                        pasteRepository.save(paste);
                    })
                    .join();
            return new KeyData(generatedKey);
        } finally {
            Files.deleteIfExists(tempFilePath);
        }
    }

    private Paste mapToPaste(PasteData pasteData, String generatedKey) {
        Paste paste = new Paste();
        paste.setCreateDate(LocalDate.now());
        paste.setEndDate(pasteData.getEndDate());
        paste.setKey(generatedKey);
        return paste;
    }

    /**
     * Извлекает {@code PasteData}, связанный с заданным ключом, скачивая соответствующий файл из S3.
     *
     * @param key уникальный ключ, используемый для поиска {@code PasteData}.
     * @return {@code PasteData} содержащий текст и дату окончания.
     * @throws IOException           если происходит ошибка при работе с файловой системой.
     * @throws PasteServiceException если {@code Paste} с указанным ключом не найден или возникает ошибка при скачивании файла.
     * @throws S3ServiceException если удаление файла из S3 завершается с ошибкой.
     */
    public PasteData findByKey(String key) throws IOException, PasteServiceException {
        Path tempFilePath = Files.createTempFile(key, ".tmp");
        try {
            Paste paste = pasteRepository.findByKey(key)
                    .orElseThrow(() -> new PasteServiceException("Paste with this key not found"));

            s3Service.downloadFile(tempFilePath, key)
                    .join();

            PasteData pasteData = new PasteData();
            pasteData.setEndDate(paste.getEndDate());
            pasteData.setText(textFileService.readFileToString(tempFilePath));

            return pasteData;
        } finally {
            Files.deleteIfExists(tempFilePath);
        }
    }

    /**
     * Удаляет {@code Paste} по указанному ключу из репозитория и соответствующий файл из хранилища S3.
     *
     * <p>Этот метод выполняет следующие шаги:
     * <ol>
     *     <li>Проверяет, что {@code key} не является {@code null} и не пустым.</li>
     *     <li>Удаляет запись {@code Paste} из репозитория по заданному ключу.</li>
     *     <li>Асинхронно удаляет файл из S3 и ожидает завершения операции.</li>
     * </ol>
     *
     * @param key уникальный ключ, используемый для удаления {@code Paste} и соответствующего файла в S3.
     * @throws IllegalArgumentException если {@code key} равен {@code null} или пустой строке.
     * @throws S3ServiceException если удаление файла из S3 завершается с ошибкой.
     */
    @Transactional
    public void delete(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key must not be null or empty");
        }
        pasteRepository.deleteByKey(key);
        s3Service.deleteFile(key)
                .join();
    }
}
