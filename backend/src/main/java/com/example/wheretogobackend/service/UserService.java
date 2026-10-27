package com.example.wheretogobackend.service;

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
                .orElseThrow(() -> new RuntimeException("Пользователь с таким логином не найден"));
    }

    // Метод входа / проверки пароля с выбросом исключений
    public boolean checkPassword(String login, String rawPassword) {
        // 1. Находим пользователя (если его нет, getUser сразу выбросит RuntimeException)
        UserEntity user = getUser(login);

        // 2. Достаем хэш из базы
        String storedHash = user.getPassword();

        // 3. Сравниваем введенный пароль с хэшем
        if (!passwordEncoder.matches(rawPassword, storedHash)) {
            throw new RuntimeException("Неверный пароль");
        }

        // Если всё ок
        return true;
    }
}