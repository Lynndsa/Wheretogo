package com.example.wheretogobackend.controller;

import com.example.wheretogobackend.dto.LoginRequest;
import com.example.wheretogobackend.dto.RegisterRequest;
import com.example.wheretogobackend.service.RegisterService;
import com.example.wheretogobackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RegisterService registerService;

    // Вход
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        boolean isAuthenticated = userService.checkPassword(
                request.getLogin(),
                request.getPassword()
        );

        if (isAuthenticated) {
            return ResponseEntity.ok("Вход успешно выполнен!");
        }

        return ResponseEntity.badRequest()
                .body("Ошибка авторизации");
    }

    // Регистрация
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String message = bindingResult.getFieldErrors()
                    .get(0)
                    .getDefaultMessage();

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "code", "VALIDATION_ERROR",
                            "message", message
                    ));
        }

        registerService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Регистрация прошла успешно!");
    }
}