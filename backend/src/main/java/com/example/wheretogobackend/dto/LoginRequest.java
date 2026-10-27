package com.example.wheretogobackend.dto;

public class LoginRequest {
    private String login;
    private String password;

    // Пустой конструктор нужен Spring для десериализации JSON
    public LoginRequest() {
    }

    // Конструктор со всеми полями (если понадобится)
    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    // Геттеры и сеттеры
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}