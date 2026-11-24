
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
        UserEntity user = findUser(idUser);
        UserInfoEntity info = findUserInfo(user.getIdUser());

        return createProfileResponse(user, info);
    }

    // Обновление профиля
    @Transactional
    public ProfileResponse updateProfile(
            Long currentIdUser,
            ProfileUpdateRequest request
    ) {
        UserEntity user = findUser(currentIdUser);
        UserInfoEntity info = findUserInfo(user.getIdUser());

        String newLogin = request.getLogin().trim();
        String name = request.getName().trim();
        String lastname = request.getLastname() == null
                ? null : request.getLastname().trim();
        String surname = request.getSurname() == null
                ? null : request.getSurname().trim();
        String phone = request.getPhonenumber().trim();
        String email = request.getEmail().trim();

        validateRequiredFields(newLogin, name, phone, email);
        validateLogin(newLogin, user.getIdUser());
        validatePhone(phone, user.getIdUser());
        validateEmail(email, user.getIdUser());

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

    private UserEntity findUser(Long idUser) {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new RegistrationException(
                        "USER_NOT_FOUND",
                        "Пользователь не найден"
                ));
    }

    private UserInfoEntity findUserInfo(Long idUser) {
        return userInfoRepository.findById(idUser)
                .orElseThrow(() -> new RegistrationException(
                        "PROFILE_NOT_FOUND",
                        "Информация профиля не найдена"
                ));
    }

    private void validateRequiredFields(
            String login,
            String name,
            String phone,
            String email
    ) {
        if (login.isEmpty() || name.isEmpty()
                || phone.isEmpty() || email.isEmpty()) {
            throw new RegistrationException(
                    "EMPTY_FIELD",
                    "Заполните обязательные поля"
            );
        }
    }

    private void validateLogin(String login, Long idUser) {
        if (userRepository.existsByLoginAndIdUserNot(login, idUser)) {
            throw new RegistrationException(
                    "LOGIN_ERROR",
                    "Пользователь с таким логином уже существует"
            );
        }
    }

    private void validatePhone(String phone, Long idUser) {
        if (userInfoRepository.existsByPhonenumberAndIdUserNot(
                phone, idUser)) {
            throw new RegistrationException(
                    "PHONE_ERROR",
                    "Этот номер телефона уже зарегистрирован"
            );
        }
    }

    private void validateEmail(String email, Long idUser) {
        if (userInfoRepository.existsByEmailAndIdUserNot(email, idUser)) {
            throw new RegistrationException(
                    "EMAIL_ERROR",
                    "Эта почта уже зарегистрирована"
            );
        }
    }

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
