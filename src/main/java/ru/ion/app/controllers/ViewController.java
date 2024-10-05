package ru.ion.app.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ion.app.DTO.KeyData;
import ru.ion.app.DTO.PasteData;
import ru.ion.app.services.paste.impl.PasteService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
@Controller

public class ViewController {

    private final PasteService pasteService;

    @GetMapping("/paste")
    public String showCreateForm(Model model) {
        model.addAttribute("pasteData", new PasteData());
        return "create_paste";
    }

    @PostMapping("/save")
    public String savePaste(@Valid @ModelAttribute("pasteData") PasteData paste, Model model, BindingResult result) throws NoSuchAlgorithmException, IOException {
        if (result.hasErrors()) {
            return "create_paste";
        }
        KeyData key = pasteService.saveToCloud(paste);
        model.addAttribute("key", key);

        return "view_key";
    }

    @GetMapping("/paste/{key}")
    public String getPaste(@PathVariable String key, Model model) throws IOException {
        PasteData paste = pasteService.findByKey(key);
        model.addAttribute("paste", paste);
        model.addAttribute("key", new KeyData(key));
        return "view_paste";
    }
}