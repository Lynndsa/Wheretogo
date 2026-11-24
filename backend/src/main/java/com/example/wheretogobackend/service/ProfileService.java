
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

        return createProfileResponse(user, info);
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
        String name = trimRequired(request.getName());
        String lastname = trimOptional(request.getLastname());
        String surname = trimOptional(request.getSurname());
        String phone = trimRequired(request.getPhonenumber());
        String email = trimRequired(request.getEmail());

        validateProfileData(newLogin, name, phone, email, user.getIdUser());

        user.setLogin(newLogin);
        userRepository.save(user);

        info.setName(name);
        info.setLastname(lastname);
        info.setSurname(surname);
        info.setPhonenumber(phone);
        info.setEmail(email);

        userInfoRepository.save(info);

        return createProfileResponse(user, info);
    }

    // Проверка обязательных полей и уникальности данных

    // Проверка обязательных полей и уникальности данных
    private void validateProfileData(
            String login,
            String name,
            String phone,
            String email,
            Long userId
    ) {
        validateRequiredFields(login, name, phone, email);
        validateLoginUniqueness(login, userId);
        validatePhoneUniqueness(phone, userId);
        validateEmailUniqueness(email, userId);
    }

    private void validateRequiredFields(
            String login,
            String name,
            String phone,
            String email
    ) {
        if (isMissing(login) || isMissing(name)
                || isMissing(phone) || isMissing(email)) {
            throw new RegistrationException(
                    "EMPTY_FIELD",
                    "Заполните обязательные поля"
            );
        }
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank();
    }

    private void validateLoginUniqueness(String login, Long userId) {
        if (userRepository.existsByLoginAndIdUserNot(login, userId)) {
            throw new RegistrationException(
                    "LOGIN_ERROR",
                    "Пользователь с таким логином уже существует"
            );
        }
    }

    private void validatePhoneUniqueness(String phone, Long userId) {
        if (userInfoRepository.existsByPhonenumberAndIdUserNot(phone, userId)) {
            throw new RegistrationException(
                    "PHONE_ERROR",
                    "Этот номер телефона уже зарегистрирован"
            );
        }
    }

    private void validateEmailUniqueness(String email, Long userId) {
        if (userInfoRepository.existsByEmailAndIdUserNot(email, userId)) {
            throw new RegistrationException(
                    "EMAIL_ERROR",
                    "Эта почта уже зарегистрирована"
            );
        }
    }

    // Обработка обязательных строковых полей
    private String trimRequired(String value) {
        return value == null ? null : value.trim();
    }

    // Обработка необязательных строковых полей
    private String trimOptional(String value) {
        return value == null ? null : value.trim();
    }

    // Формирование ответа с данными профиля
    private ProfileResponse createProfileResponse(
            UserEntity user,
            UserInfoEntity info
    ) {
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
