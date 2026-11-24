package com.example.wheretogobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Стандартная точка входа Spring Boot, не является утилитным классом.
@SuppressWarnings("PMD.UseUtilityClass")
@SpringBootApplication
public class WhereToGoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhereToGoBackendApplication.class, args);
    }
}
