package com.example.wheretogobackend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Введите имя")
    @Size(max = 20, message = "Имя не длиннее 20 символов")
    private String name;

    @NotBlank(message = "Введите номер телефона")
    @Pattern(regexp = "\\d{11}", message = "Телефон должен состоять из 11 цифр")
    private String phonenumber;

    @NotBlank(message = "Введите почту")
    @Email(message = "Некорректная почта")
    @Size(max = 150, message = "Почта слишком длинная")
    private String email;

    @NotBlank(message = "Введите пароль")
    @Size(min = 6, max = 72, message = "Пароль от 6 до 72 символов")
    private String password;

    @NotBlank(message = "Повторите пароль")
    private String confirmPassword;
}