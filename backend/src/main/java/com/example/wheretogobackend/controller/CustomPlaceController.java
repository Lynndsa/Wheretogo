package com.example.wheretogobackend.controller;

import com.example.wheretogobackend.mysql.entity.DataEntity;
import com.example.wheretogobackend.service.CustomPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/custom-places")
@RequiredArgsConstructor
public class CustomPlaceController {

    private final CustomPlaceService customPlaceService;

    // получение всех мест
    @GetMapping
    public List<DataEntity> getAllPlaces() {
        return customPlaceService.getAllPlaces();
    }

    // получение по ID
    @GetMapping("/{id}")
    public ResponseEntity<DataEntity> getPlaceById(@PathVariable Long id) {
        return customPlaceService.getPlaceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}