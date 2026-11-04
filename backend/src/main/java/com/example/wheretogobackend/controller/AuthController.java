package com.example.wheretogobackend.controller;

import com.example.wheretogobackend.dto.LoginRequest;
import com.example.wheretogobackend.dto.LoginResponse;
import com.example.wheretogobackend.dto.RegisterRequest;
import com.example.wheretogobackend.exception.RegistrationException;
import com.example.wheretogobackend.mysql.entity.UserEntity;
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
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        UserEntity user = userService.authenticate(
                request.getLogin(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                new LoginResponse(
                        user.getIdUser(),
                        user.getLogin()
                )
        );
    }

    // Регистрация
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            String message = bindingResult.getFieldErrors()
                    .get(0)
                    .getDefaultMessage();

            throw new RegistrationException(
                    "VALIDATION_ERROR",
                    message
            );
        }

        UserEntity user = registerService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse(
                        user.getIdUser(),
                        user.getLogin()
                ));
    }
}