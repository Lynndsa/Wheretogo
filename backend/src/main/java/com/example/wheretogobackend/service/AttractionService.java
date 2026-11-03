
package com.example.wheretogobackend.service;

import com.example.wheretogobackend.dto.AttractionDto;
import com.example.wheretogobackend.dto.GeoapifyResponseDto;
import com.example.wheretogobackend.mysql.entity.DataEntity;
import com.example.wheretogobackend.mysql.repository.DataRepository;
import com.example.wheretogobackend.tools.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final DataRepository dataRepository;
    private final GeoapifyService geoapifyService;
    private final WikipediaService wikipediaService;

    public List<AttractionDto> getNearbyPlaces(
            Double lat,
            Double lon,
            Double radiusInMeters
    ) {
        GeoapifyResponseDto geoapifyData =
                geoapifyService.getNearbyPlaces(lat, lon, radiusInMeters);

        List<AttractionDto> attractions = new ArrayList<>();

        if (geoapifyData == null || geoapifyData.getFeatures() == null) {
            return attractions;
        }

        for (GeoapifyResponseDto.Feature feature : geoapifyData.getFeatures()) {

            GeoapifyResponseDto.Properties props = feature.getProperties();

            if (props == null || props.getName() == null
                    || props.getName().isBlank()) {
                continue;
            }

            String title = props.getName();
            Double itemLat = props.getLat();
            Double itemLon = props.getLon();

            if (itemLat == null || itemLon == null) {
                continue;
            }

            String category =
                    (props.getCategories() != null
                            && !props.getCategories().isEmpty())
                            ? props.getCategories().get(0)
                            : "attraction";

            String imageUrl = null;
            String description = null;
            String wikipediaUrl = props.getWiki();

            String wikiTag = extractWikiTag(props);

            if (wikiTag != null && !wikiTag.isEmpty()) {
                Map<String, String> wikiData =
                        wikipediaService.getWikiData(wikiTag);

                description = wikiData.get("description");
                imageUrl = wikiData.get("imageUrl");

                if (wikipediaUrl == null) {
                    wikipediaUrl = wikiData.get("wikiUrl");
                }
            }

            if (description == null) {
                continue;
            }

            AttractionDto dto = AttractionDto.builder()
                    .title(title)
                    .latitude(itemLat)
                    .longitude(itemLon)
                    .category(category)
                    .address(props.getAddress())
                    .websiteUrl(props.getWebsite())
                    .description(description)
                    .imageUrl(imageUrl)
                    .wikipediaUrl(wikipediaUrl)
                    .build();

            attractions.add(dto);
        }

        return attractions;
    }

    private String extractWikiTag(
            GeoapifyResponseDto.Properties props
    ) {
        if (props.getDatasource() != null
                && props.getDatasource().getRaw() != null) {
            return props.getDatasource().getRaw().getWikipedia();
        }

        return props.getWiki();
    }

    public List<AttractionDto> getNearbyPlacesStream(
            double lat,
            double lon,
            double radiusMeters
    ) {
        double maxAllowedRadius = 20000.0;

        if (radiusMeters > maxAllowedRadius || radiusMeters < 0) {
            return Collections.emptyList();
        }

        List<DataEntity> allPlaces = dataRepository.findAll();

        Map<String, DataEntity> uniquePlaces = new LinkedHashMap<>();

        allPlaces.stream()
                .filter(place ->
                        GeoUtils.calculateDistanceInMeters(
                                lat,
                                lon,
                                place.getLatitude(),
                                place.getLongitude()
                        ) <= radiusMeters
                )
                .forEach(place -> {
                    String key = place.getName().trim().toLowerCase()
                            + "_" + place.getLatitude()
                            + "_" + place.getLongitude();

                    uniquePlaces.putIfAbsent(key, place);
                });

        return uniquePlaces.values().stream()
                .map(place -> {
                    Map<String, String> wikiData =
                            wikipediaService.getWikiData(place.getName());

                    String wikiDesc = wikiData.get("description");

                    String finalDescription;

                    if (wikiDesc != null && !wikiDesc.isBlank()) {
                        finalDescription = wikiDesc;
                    } else if (place.getDescription() != null
                            && !place.getDescription().isBlank()) {
                        finalDescription = place.getDescription();
                    } else {
                        finalDescription = "Описание отсутствует";
                    }

                    String wikiImage = wikiData.get("imageUrl");

                    String finalImage =
                            (wikiImage != null && !wikiImage.isBlank())
                                    ? wikiImage
                                    : place.getImageUrl();

                    System.out.println(
                            "Успешно обработано место: " + place.getName()
                    );

                    return AttractionDto.builder()
                            .title(place.getName())
                            .address(place.getAddress())
                            .latitude(place.getLatitude())
                            .longitude(place.getLongitude())
                            .category(place.getCategory())
                            .description(finalDescription)
                            .imageUrl(finalImage)
                            .websiteUrl(place.getWebsiteUrl())
                            .wikipediaUrl(wikiData.get("wikiUrl"))
                            .build();
                })
                .toList();
    }

    public List<AttractionDto> getAllNearbyPlaces(
            double lat,
            double lon,
            double radiusMeters
    ) {
        List<AttractionDto> combinedList = new ArrayList<>();

        // 1. Получаем места из MySQL
        List<AttractionDto> dbPlaces =
                getNearbyPlacesStream(lat, lon, radiusMeters);

        if (dbPlaces != null) {
            combinedList.addAll(dbPlaces);
        }

        // 2. Получаем места из Geoapify
        List<AttractionDto> apiPlaces =
                getNearbyPlaces(lat, lon, radiusMeters);

        if (apiPlaces != null) {
            combinedList.addAll(apiPlaces);
        }

        // 3. Удаляем дубликаты между двумя источниками
        List<AttractionDto> uniquePlaces = new ArrayList<>();

        for (AttractionDto place : combinedList) {

            if (place.getLatitude() == null
                    || place.getLongitude() == null) {
                uniquePlaces.add(place);
                continue;
            }

            boolean duplicate = uniquePlaces.stream().anyMatch(existing -> {

                if (existing.getLatitude() == null
                        || existing.getLongitude() == null
                        || existing.getTitle() == null
                        || place.getTitle() == null) {
                    return false;
                }

                boolean sameName =
                        existing.getTitle().trim().equalsIgnoreCase(
                                place.getTitle().trim()
                        );

                double distance = GeoUtils.calculateDistanceInMeters(
                        existing.getLatitude(),
                        existing.getLongitude(),
                        place.getLatitude(),
                        place.getLongitude()
                );

                return sameName && distance <= 20;
            });

            if (!duplicate) {
                uniquePlaces.add(place);
            }
        }

        return uniquePlaces;
    }

    public AttractionDto getPlaceByCoordinates(
            Double lat,
            Double lon
    ) {
        List<DataEntity> allPlaces = dataRepository.findAll();

        for (DataEntity place : allPlaces) {

            if (Math.abs(place.getLatitude() - lat) < 0.0001
                    && Math.abs(place.getLongitude() - lon) < 0.0001) {

                Map<String, String> wikiData =
                        wikipediaService.getWikiData(place.getName());

                String finalDescription =
                        (wikiData.get("description") != null
                                && !wikiData.get("description").isBlank())
                                ? wikiData.get("description")
                                : place.getDescription();

                String finalImage =
                        (wikiData.get("imageUrl") != null
                                && !wikiData.get("imageUrl").isBlank())
                                ? wikiData.get("imageUrl")
                                : place.getImageUrl();

                return AttractionDto.builder()
                        .title(place.getName())
                        .address(place.getAddress())
                        .latitude(place.getLatitude())
                        .longitude(place.getLongitude())
                        .category(place.getCategory())
                        .description(
                                finalDescription != null
                                        ? finalDescription
                                        : "Описание отсутствует"
                        )
                        .imageUrl(finalImage)
                        .websiteUrl(place.getWebsiteUrl())
                        .wikipediaUrl(wikiData.get("wikiUrl"))
                        .build();
            }
        }

        return null;
    }
}
