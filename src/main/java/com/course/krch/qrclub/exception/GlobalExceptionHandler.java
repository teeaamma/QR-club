package com.course.krch.qrclub.exception;

import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleNotFound(NotFoundException ex){
        return ResponseEntity.status(404).body(
                new SimpleErrorResponse(ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleDtoValidation(MethodArgumentNotValidException ex){
        List<Violation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(violation -> new Violation(violation.getField(), violation.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(new ValidationErrorResponse(violations));
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<SimpleErrorResponse> handleInvalidEntityProperty(PropertyReferenceException ex){
        return ResponseEntity.badRequest().body(
                new SimpleErrorResponse("Поле %s не существует".formatted(ex.getPropertyName()))
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<SimpleErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex){
        return ResponseEntity.badRequest().body(
                new SimpleErrorResponse("Неправильный формат аргумента " + ex.getName())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<SimpleErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex){
        return ResponseEntity.badRequest().body(
                new SimpleErrorResponse("Неправильный формат тела запроса")
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<SimpleErrorResponse> handleMethodNotSupported(){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                new SimpleErrorResponse("Метод не поддерживается")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleUnexpected(Exception ex){
        return ResponseEntity.status(500).body(
                new SimpleErrorResponse("Внутренняя ошибка сервера")
        );
    }
}
