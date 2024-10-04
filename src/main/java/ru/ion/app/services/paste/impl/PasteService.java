package ru.ion.app.services.paste.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ion.app.DTO.KeyData;
import ru.ion.app.DTO.PasteData;
import ru.ion.app.entitys.Paste;
import ru.ion.app.exception.PasteServiceException;
import ru.ion.app.exception.S3ServiceException;
import ru.ion.app.mapper.PasteMapper;
import ru.ion.app.repositories.PasteRepository;
import ru.ion.app.services.keyGeneration.impl.KeyGenerationService;
import ru.ion.app.services.s3.impl.S3Service;
import ru.ion.app.services.textFile.impl.TextFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PasteService {


    private final S3Service s3Service;
    private final TextFileService textFileService;
    private final KeyGenerationService keyGenerationService;
    private final PasteRepository pasteRepository;
    private final PasteMapper pasteMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);
    private static final String ACCESS_COUNT_PREFIX = "access_count:";

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
     * @throws S3ServiceException       если удаление файла из S3 завершается с ошибкой.
     */

    public KeyData saveToCloud(PasteData pasteData) throws NoSuchAlgorithmException, IOException {
        String generatedKey = keyGenerationService.generateKey();
        Path tempFilePath = Files.createTempFile(generatedKey, ".tmp");

        try {
            Paste paste = pasteMapper.toPaste(pasteData, generatedKey, LocalDate.now());
            textFileService.writeStringToFile(pasteData.getText(), tempFilePath);
            pasteRepository.save(paste);

            s3Service.uploadFile(tempFilePath, generatedKey)
                    .join();

            return new KeyData(generatedKey);
        } finally {
            Files.deleteIfExists(tempFilePath);
        }
    }

    /**
     * Находит данные пасты по заданному ключу.
     * <p>
     * Метод выполняет поиск пасты в репозитории по ключу. Если паста найдена, пытается
     * получить закэшированный текст из Redis. Если текст не найден в кеше, скачивает
     * его из S3, кэширует при необходимости и возвращает данные пасты.
     *
     * @param key уникальный ключ пасты
     * @return {@link PasteData} содержащие текст пасты и дату окончания
     * @throws IOException если возникает ошибка при скачивании или чтении файла
     * @throws PasteServiceException если паста с заданным ключом не найдена
     */
    public PasteData findByKey(String key) throws IOException {
        Paste paste = pasteRepository.findByKey(key)
                .orElseThrow(() -> new PasteServiceException("Paste with this key not found"));

        String cachedText = redisTemplate.opsForValue().get(key);

        if (cachedText != null) {
            return new PasteData(cachedText, paste.getEndDate());
        } else {
            String text = downloadAndReadText(key);
            if (shouldCashed(key)) redisTemplate.opsForValue().set(key, text, CACHE_TTL);

            return new PasteData(text, paste.getEndDate());
        }
    }
    /**
     * Определяет, следует ли кэшировать текст пасты.
     * <p>
     * Метод увеличивает счетчик доступа для заданного ключа в Redis. Если количество
     елей
     * превышает пороговое значение, возвращает {@code true}, указывая на необходимость кэширования.
     *
     * @param key уникальный ключ пасты
     * @return {@code true}, если количество доступов превышает 10, иначе {@code false}
     */
    private boolean shouldCashed(String key) {
        String accessCountKey = ACCESS_COUNT_PREFIX + key;
        Long accessCount = redisTemplate.opsForValue().increment(accessCountKey, 1);
        if (accessCount != null && accessCount == 1)
            redisTemplate.expire(accessCountKey, Duration.ofMinutes(10));

        return accessCount != null && accessCount > 10;
    }
    /**
     * Скачивает и читает текст пасты из S3.
     * <p>
     * Метод создает временный файл, скачивает содержимое из S3 по заданному ключу,
     * читает содержимое файла в строку и удаляет временный файл.
     *
     * @param key уникальный ключ пасты
     * @return содержимое пасты в виде строки
     * @throws IOException если возникает ошибка при работе с файловой системой или скачивании файла
     */
    private String downloadAndReadText(String key) throws IOException {
        Path tempFilePath = Files.createTempFile(key, ".tmp");
        try {
            s3Service.downloadFile(tempFilePath, key)
                    .join();

            return textFileService.readFileToString(tempFilePath);
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
     * @throws S3ServiceException       если удаление файла из S3 завершается с ошибкой.
     */
    @Transactional
    public void deleteByKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key must not be null or empty");
        }
        pasteRepository.deleteByKey(key);
        s3Service.deleteFile(key)
                .join();
    }

    @Transactional
    public void deleteExpiredPaste() {
        pasteRepository.findByEndDateBefore(LocalDate.now())
                .forEach(paste -> deleteByKey(paste.getKey()));
    }
}
