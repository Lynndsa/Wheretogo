package com.example.wheretogobackend.mysql.repository;

import com.example.wheretogobackend.mysql.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // проверка на существование юзра
    boolean existsByLogin(String login);
    // берем его логин
    Optional<UserEntity> findByLogin(String login);

}
