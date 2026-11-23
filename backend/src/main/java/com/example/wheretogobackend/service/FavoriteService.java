package com.example.wheretogobackend.service;

import com.example.wheretogobackend.mysql.entity.Favorite;
import com.example.wheretogobackend.mysql.entity.FavoriteId;
import com.example.wheretogobackend.mysql.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository repository;

    public FavoriteService(FavoriteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Long> getFavoriteAttractionIds(Long userId) {
        return repository.findByIdIdUser(userId).stream()
                .map(f -> f.getId().getIdAttraction())
                .toList();
    }

    @Transactional
    public void add(Long userId, Long attractionId) {
        FavoriteId id = new FavoriteId(userId, attractionId);
        if (!repository.existsById(id)) {
            repository.save(new Favorite(id));
        }
    }

    @Transactional
    public void remove(Long userId, Long attractionId) {
        FavoriteId id = new FavoriteId(userId, attractionId);
        if (repository.existsById(id)) {
            repository.deleteById(id);
        }
    }
}