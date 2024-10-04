package ru.ion.app.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.ion.app.DTO.KeyData;
import ru.ion.app.DTO.PasteData;
import ru.ion.app.exception.PasteServiceException;
import ru.ion.app.services.paste.impl.PasteService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
@RestController()
public class AppController {

    private final PasteService pasteService;

    @PostMapping("/save")
    public KeyData savePaste(@Valid @RequestBody PasteData paste) throws NoSuchAlgorithmException, IOException {
        return pasteService.saveToCloud(paste);
    }

    @GetMapping(path = {"/{key}"})
    public PasteData getPaste(@PathVariable String key) throws IOException {
        return pasteService.findByKey(key);
    }

    @DeleteMapping("/{key}/delete")
    public void deletePaste(@PathVariable String key) {
        pasteService.deleteByKey(key);
    }


}