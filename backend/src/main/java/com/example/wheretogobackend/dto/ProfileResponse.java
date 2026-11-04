package com.example.wheretogobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {

    private String login;
    private String name;
    private String lastname;
    private String surname;
    private String phonenumber;
    private String email;
}