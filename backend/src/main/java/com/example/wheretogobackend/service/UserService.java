package com.example.wheretogobackend.service;

import com.example.wheretogobackend.exception.RegistrationException;
import com.example.wheretogobackend.mysql.entity.UserEntity;
import com.example.wheretogobackend.mysql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean existUser(String login) {
        return userRepository.existsByLogin(login);
    }

    public UserEntity getUser(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new RegistrationException("LOGIN_NOT_EXIST","Пользователь с таким логином не найден"));
    }

    // Метод авторизации
    public UserEntity authenticate(String login, String rawPassword) {

        UserEntity user = getUser(login);

        String storedHash = user.getPassword();

        if (!passwordEncoder.matches(rawPassword, storedHash)) {
            throw new RegistrationException(
                    "PASSWORD_EXIST",
                    "Неверный пароль"
            );
        }

        return user;
    }
}