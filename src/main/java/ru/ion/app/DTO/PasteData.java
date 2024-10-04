package ru.ion.app.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PasteData {
    @NotBlank(message = "text cannot be empty")
    private String text;

    @Future(message = "date should not be earlier than tomorrow")
    private LocalDate endDate;
}
