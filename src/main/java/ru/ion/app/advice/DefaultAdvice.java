package ru.ion.app.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.ion.app.DTO.ResponseData;
import ru.ion.app.exception.PasteServiceException;
import ru.ion.app.exception.S3ServiceException;


@ControllerAdvice
public class DefaultAdvice {

    @ExceptionHandler({PasteServiceException.class, S3ServiceException.class})
    public String handleCustomExceptions(Exception e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error";
    }

}