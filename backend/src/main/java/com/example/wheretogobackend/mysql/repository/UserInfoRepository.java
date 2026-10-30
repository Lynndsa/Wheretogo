package com.example.wheretogobackend.mysql.repository;

import com.example.wheretogobackend.mysql.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, Long> {
    boolean existsByPhonenumber(String phonenumber);
    boolean existsByEmail(String email);
}