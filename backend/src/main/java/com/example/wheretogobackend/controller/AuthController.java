package com.example.wheretogobackend.controller;

import com.example.wheretogobackend.dto.LoginRequest;
import com.example.wheretogobackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            boolean isAuthenticated = userService.checkPassword(request.getLogin(), request.getPassword());

            if (isAuthenticated) {
                return ResponseEntity.ok("Вход успешно выполнен!");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.badRequest().body("Ошибка авторизации");
    }
}