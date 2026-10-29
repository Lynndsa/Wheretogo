package com.example.wheretogobackend.service;

import com.example.wheretogobackend.dto.RegisterRequest;
import com.example.wheretogobackend.exception.RegistrationException;
import com.example.wheretogobackend.mysql.entity.UserEntity;
import com.example.wheretogobackend.mysql.entity.UserInfoEntity;
import com.example.wheretogobackend.mysql.repository.UserInfoRepository;
import com.example.wheretogobackend.mysql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest req) {
        String name = req.getName().trim();
        String email = req.getEmail().trim();

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new RegistrationException("PASSWORD_ERROR","Пароли не совпадают");
        }
        if (userRepository.existsByLogin(name)) {
            throw new RegistrationException("LOGIN_ERROR","Пользователь с таким именем уже есть");
        }
        if (userInfoRepository.existsByPhonenumber(req.getPhonenumber())) {
            throw new RegistrationException("PHONE_ERROR","Этот номер телефона уже зарегистрирован");
        }
        if (userInfoRepository.existsByEmail(email)) {
            throw new RegistrationException("EMAIL_ERROR","Эта почта уже зарегистрирована");
        }

        UserEntity user = userRepository.save(
                UserEntity.builder()
                        .login(name)
                        .password(passwordEncoder.encode(req.getPassword()))
                        .build()
        );

        UserInfoEntity info = new UserInfoEntity();
        info.setUser(user);
        info.setName(name);
        info.setPhonenumber(req.getPhonenumber());
        info.setEmail(email);
        userInfoRepository.save(info);
    }
}