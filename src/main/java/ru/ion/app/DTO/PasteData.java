package ru.ion.app.DTO;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
public class PasteData{
    private String text;
    private LocalDate endDate;
}
