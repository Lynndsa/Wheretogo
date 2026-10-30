
package com.example.wheretogobackend.exception;

import com.example.wheretogobackend.exception.RegistrationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Map<String, String>> handleRegistrationException(
            RegistrationException ex) {

        String code = ex.getCode();

        HttpStatus status = switch (code) {
            case "LOGIN_EXISTS",
                 "EMAIL_EXISTS",
                 "PHONE_EXISTS",
                 "LOGIN_ERROR",
                 "EMAIL_ERROR",
                 "PHONE_ERROR" -> HttpStatus.CONFLICT;

            case "LOGIN_NOT_EXIST",
                 "PASSWORD_EXIST" -> HttpStatus.UNAUTHORIZED;

            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status)
                .body(Map.of(
                        "code", code,
                        "message", ex.getMessage()
                ));
    }
}