package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.exceptions.AuthenticationApplicationException;
import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.InvalidInputDataException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.rest.dto.ExceptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.time.LocalDateTime;

import static java.util.stream.Collectors.joining;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {
    @ExceptionHandler({ValidationException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            HttpRequestMethodNotSupportedException.class,
            InvalidInputDataException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<?> invalidInputError(HttpServletRequest request, Exception e) {
        log.error("Request: " + request.getRequestURL() + " raised " + e.getMessage(), e);
        HttpStatus errorStatus = HttpStatus.BAD_REQUEST;
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .timestamp(LocalDateTime.now())
                .status(errorStatus.value())
                .error(e.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(errorStatus).body(exceptionDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> MethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        log.error("Request: " + request.getRequestURL() + " raised " + e.getMessage(), e);
        HttpStatus errorStatus = HttpStatus.BAD_REQUEST;
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError ->
                        String.format("field [%s] is not valid: %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(joining("; "));
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .timestamp(LocalDateTime.now())
                .status(errorStatus.value())
                .error(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(errorStatus).body(exceptionDto);
    }

    @ExceptionHandler(NotFoundCustomApplicationException.class)
    public ResponseEntity<?> notFoundError(HttpServletRequest request, Exception e) {
        log.error("Request: " + request.getRequestURL() + " raised " + e.getMessage(), e);
        HttpStatus errorStatus = HttpStatus.NOT_FOUND;
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .timestamp(LocalDateTime.now())
                .status(errorStatus.value())
                .error(e.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(errorStatus).body(exceptionDto);
    }

    @ExceptionHandler(AuthenticationApplicationException.class)
    public ResponseEntity<?> authError(HttpServletRequest request, Exception e) {
        log.error("Request: " + request.getRequestURL() + " raised " + e.getMessage(), e);
        HttpStatus errorStatus = HttpStatus.UNAUTHORIZED;
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .timestamp(LocalDateTime.now())
                .status(errorStatus.value())
                .error(e.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(errorStatus).body(exceptionDto);
    }

    @ExceptionHandler(value = {CustomApplicationException.class, Exception.class})
    public ResponseEntity<?> applicationError(HttpServletRequest request, Exception e) {
        log.error("Request: " + request.getRequestURL() + " raised " + e.getMessage(), e);
        HttpStatus errorStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .timestamp(LocalDateTime.now())
                .status(errorStatus.value())
                .error("Internal Server Error")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(errorStatus).body(exceptionDto);
    }
}
