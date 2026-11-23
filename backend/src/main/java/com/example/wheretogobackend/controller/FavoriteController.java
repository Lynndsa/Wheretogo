package com.example.wheretogobackend.controller;

import com.example.wheretogobackend.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService service;

    public FavoriteController(FavoriteService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public List<Integer> getFavorites(@PathVariable Long userId) {
        return service.getFavoriteAttractionIds(userId);
    }

    @PostMapping("/{userId}/{attractionId}")
    public ResponseEntity<Void> add(@PathVariable Long userId,
                                    @PathVariable int attractionId) {
        service.add(userId, attractionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/{attractionId}")
    public ResponseEntity<Void> remove(@PathVariable Long userId,
                                       @PathVariable int attractionId) {
        service.remove(userId, attractionId);
        return ResponseEntity.ok().build();
    }
}