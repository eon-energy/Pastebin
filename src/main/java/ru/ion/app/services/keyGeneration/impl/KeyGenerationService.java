package ru.ion.app.services.keyGeneration.impl;

import org.springframework.stereotype.Service;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class KeyGenerationService {
    public String generateRandomString(int length) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(randomBytes);

        String encoded = Base64.getUrlEncoder().encodeToString(hash);
        return encoded.substring(0, Math.min(length, encoded.length()));
    }

    public String generateKey() throws NoSuchAlgorithmException {
        String generatedString = generateRandomString(8);

        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedDateTime = now.format(formatter);

        return generatedString + formattedDateTime;
    }

}
