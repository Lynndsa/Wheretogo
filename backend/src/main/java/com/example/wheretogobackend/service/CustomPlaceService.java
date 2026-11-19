package com.example.wheretogobackend.service;

import com.example.wheretogobackend.mysql.entity.DataEntity;
import com.example.wheretogobackend.mysql.repository.DataRepository;
import com.example.wheretogobackend.tools.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomPlaceService {

    private final DataRepository dataRepository;

    // получение всех сохраненныъ мест
    public List<DataEntity> getAllPlaces() {
        return dataRepository.findAll();
    }

    // поиск места по ID
    public Optional<DataEntity> getPlaceById(Long id) {
        return dataRepository.findById(id);
    }




    public List<DataEntity> getNearbyPlaces(
            double lat,
            double lon,
            double radius
    ) {
        if (radius <= 0 || radius > 20000) {
            return Collections.emptyList();
        }

        return dataRepository.findAll().stream()
                .filter(place ->
                        place.getLatitude() != null
                                && place.getLongitude() != null
                )
                .filter(place ->
                        GeoUtils.calculateDistanceInMeters(
                                lat,
                                lon,
                                place.getLatitude(),
                                place.getLongitude()
                        ) <= radius
                )
                .toList();
    }

}