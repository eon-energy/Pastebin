package ru.ion.app.DTO;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@ToString
public class PasteData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String text;
    private LocalDate createDate;
    private LocalDate endDate;
}
