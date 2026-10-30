
package com.example.wheretogobackend.exception;

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

        HttpStatus status = HttpStatus.BAD_REQUEST;

        if ("LOGIN_EXISTS".equals(ex.getCode())
                || "EMAIL_EXISTS".equals(ex.getCode())
                || "PHONE_EXISTS".equals(ex.getCode())) {
            status = HttpStatus.CONFLICT;
        }
        else if ("LOGIN_NOT_EXIST".equals(ex.getCode())
                || "PASSWORD_EXIST".equals(ex.getCode())) {
            status = HttpStatus.UNAUTHORIZED;
        }

        return ResponseEntity.status(status)
                .body(Map.of(
                        "code", ex.getCode(),
                        "message", ex.getMessage()
                ));
    }
}
