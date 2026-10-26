package com.example.wheretogobackend.controller;

import com.example.wheretogobackend.dto.AttractionDto;
import com.example.wheretogobackend.service.AttractionService;
import com.example.wheretogobackend.service.WikipediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final AttractionService placeService;

    @GetMapping("/nearby")
    public List<AttractionDto> getNearbyPlaces(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radius
    ) {

        return placeService.getAllNearbyPlaces(lat, lon, radius);
    }

    @GetMapping("/details")
    public ResponseEntity<AttractionDto> getPlaceDetails(
            @RequestParam double lat,
            @RequestParam double lon
    ) {
        AttractionDto attraction = placeService.getPlaceByCoordinates(lat, lon);

        if (attraction == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(attraction);
    }
}