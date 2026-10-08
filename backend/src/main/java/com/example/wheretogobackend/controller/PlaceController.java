package com.example.wheretogobackend.controller;

import com.example.wheretogobackend.dto.AttractionDto;
import com.example.wheretogobackend.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final AttractionService attractionService;

    @GetMapping("/nearby")
    public ResponseEntity<List<AttractionDto>> getNearbyPlaces(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "3000") Double radius) {
        List<AttractionDto> places = attractionService.getNearbyPlaces(lat, lon, radius);
        return ResponseEntity.ok(places);
    }
}