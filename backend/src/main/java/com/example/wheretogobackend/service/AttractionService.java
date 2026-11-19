
package com.example.wheretogobackend.service;

import com.example.wheretogobackend.dto.AttractionDto;
import com.example.wheretogobackend.mysql.entity.DataEntity;
import com.example.wheretogobackend.mysql.repository.DataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AttractionService {

    private final DataRepository dataRepository;

    // Преобразование записи БД в DTO
    private AttractionDto mapDatabasePlace(DataEntity place) {

        String description = place.getDescription();

        if (description == null || description.isBlank()) {
            description = "Описание отсутствует";
        }

        return AttractionDto.builder()
                .title(place.getName())
                .description(description)
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .category(place.getCategory())
                .imageUrl(place.getImageUrl())
                .websiteUrl(place.getWebsiteUrl())
                .build();
    }
}

