package com.example.wheretogobackend.mysql.repository;



import com.example.wheretogobackend.mysql.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Проверка существования пользователя по логину
    boolean existsByLogin(String login);

    // Поиск пользователя по логину
    Optional<UserEntity> findByLogin(String login);

    // Проверка уникальности логина при редактировании профиля
    boolean existsByLoginAndIdUserNot(String login, Long idUser);
}