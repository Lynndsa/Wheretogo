
package com.example.wheretogobackend.service;

import com.example.wheretogobackend.dto.ProfileResponse;
import com.example.wheretogobackend.dto.ProfileUpdateRequest;
import com.example.wheretogobackend.exception.RegistrationException;
import com.example.wheretogobackend.mysql.entity.UserEntity;
import com.example.wheretogobackend.mysql.entity.UserInfoEntity;
import com.example.wheretogobackend.mysql.repository.UserInfoRepository;
import com.example.wheretogobackend.mysql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;

    // Получение профиля
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long idUser) {

        UserEntity user = userRepository.findById(idUser)
                .orElseThrow(() -> new RegistrationException(
                        "USER_NOT_FOUND",
                        "Пользователь не найден"
                ));

        UserInfoEntity info = userInfoRepository.findById(user.getIdUser())
                .orElseThrow(() -> new RegistrationException(
                        "PROFILE_NOT_FOUND",
                        "Информация профиля не найдена"
                ));

        return new ProfileResponse(
                user.getLogin(),
                info.getName(),
                info.getLastname(),
                info.getSurname(),
                info.getPhonenumber(),
                info.getEmail()
        );
    }

    // Обновление профиля
    @Transactional
    public ProfileResponse updateProfile(
            Long currentIdUser,
            ProfileUpdateRequest request
    ) {

        UserEntity user = userRepository.findById(currentIdUser)
                .orElseThrow(() -> new RegistrationException(
                        "USER_NOT_FOUND",
                        "Пользователь не найден"
                ));

        UserInfoEntity info = userInfoRepository.findById(user.getIdUser())
                .orElseThrow(() -> new RegistrationException(
                        "PROFILE_NOT_FOUND",
                        "Информация профиля не найдена"
                ));

        String newLogin = request.getLogin().trim();
        String name = request.getName().trim();
        String lastname = request.getLastname() == null
                ? null : request.getLastname().trim();
        String surname = request.getSurname() == null
                ? null : request.getSurname().trim();
        String phone = request.getPhonenumber().trim();
        String email = request.getEmail().trim();

        // Проверка обязательных полей
        if (newLogin.isEmpty() || name.isEmpty()
                || phone.isEmpty() || email.isEmpty()) {
            throw new RegistrationException(
                    "EMPTY_FIELD",
                    "Заполните обязательные поля"
            );
        }

        // Проверка уникальности логина
        if (userRepository.existsByLoginAndIdUserNot(
                newLogin, user.getIdUser())) {
            throw new RegistrationException(
                    "LOGIN_ERROR",
                    "Пользователь с таким логином уже существует"
            );
        }

        // Проверка уникальности телефона
        if (userInfoRepository.existsByPhonenumberAndIdUserNot(
                phone, user.getIdUser())) {
            throw new RegistrationException(
                    "PHONE_ERROR",
                    "Этот номер телефона уже зарегистрирован"
            );
        }

        // Проверка уникальности почты
        if (userInfoRepository.existsByEmailAndIdUserNot(
                email, user.getIdUser())) {
            throw new RegistrationException(
                    "EMAIL_ERROR",
                    "Эта почта уже зарегистрирована"
            );
        }

        // Обновление основной таблицы
        user.setLogin(newLogin);
        userRepository.save(user);

        // Обновление персональных данных
        info.setName(name);
        info.setLastname(lastname);
        info.setSurname(surname);
        info.setPhonenumber(phone);
        info.setEmail(email);

        userInfoRepository.save(info);

        return new ProfileResponse(
                user.getLogin(),
                info.getName(),
                info.getLastname(),
                info.getSurname(),
                info.getPhonenumber(),
                info.getEmail()
        );
    }
}
