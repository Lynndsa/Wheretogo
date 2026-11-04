package com.example.wheretogobackend.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {

    private String login;
    private String name;
    private String lastname;
    private String surname;
    private String phonenumber;
    private String email;
}