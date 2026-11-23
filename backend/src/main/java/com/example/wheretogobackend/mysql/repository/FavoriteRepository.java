package com.example.wheretogobackend.mysql.repository;

import com.example.wheretogobackend.mysql.entity.Favorite;
import com.example.wheretogobackend.mysql.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    // Favorite -> поле id (FavoriteId) -> поле idUser
    List<Favorite> findByIdIdUser(Long idUser);
}