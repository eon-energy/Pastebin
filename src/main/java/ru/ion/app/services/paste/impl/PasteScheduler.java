package ru.ion.app.services.paste.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasteScheduler {
    private final PasteService pasteService;

    @Scheduled(cron = "0 0 3 * * *") // Every day at 3:00 AM
    public void cleanupPastes(){
        pasteService.deleteExpiredPaste();
    }
}
