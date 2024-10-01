package ru.ion.app.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.ion.app.DTO.ResponseData;
import ru.ion.app.exception.PasteServiceException;


@ControllerAdvice
public class DefaultAdvice {
    @ExceptionHandler(PasteServiceException.class)
    public ResponseEntity<ResponseData> handleException(Exception e) {
        ResponseData responseData = new ResponseData(e.getMessage());
        return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData> handleValidationErrors(MethodArgumentNotValidException e) {
        ResponseData responseData = new ResponseData(e.getMessage());

        return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
    }
}
