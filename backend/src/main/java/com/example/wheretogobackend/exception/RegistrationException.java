package com.example.wheretogobackend.exception;

import lombok.Getter;

@Getter
public class RegistrationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;

    public RegistrationException(String code, String message) {
        super(message);
        this.code = code;
    }

}