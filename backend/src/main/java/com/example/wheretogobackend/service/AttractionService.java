package com.example.wheretogobackend.service;

import com.example.wheretogobackend.dto.AttractionDto;
import com.example.wheretogobackend.dto.GeoapifyResponseDto;
import com.example.wheretogobackend.mysql.entity.DataEntity;
import com.example.wheretogobackend.mysql.repository.DataRepository;
import com.example.wheretogobackend.tools.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final DataRepository dataRepository;
    private final GeoapifyService geoapifyService;
    private final WikipediaService wikipediaService;

    // Поля с инициализацией не попадают в конструктор Lombok
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private final Map<String, AttractionDto> placeCache = new ConcurrentHashMap<>();

    // Получение мест из Geoapify (параллельные запросы в Wikipedia)
    public List<AttractionDto> getNearbyPlaces(
            Double lat,
            Double lon,
            Double radiusInMeters
    ) {
        GeoapifyResponseDto data =
                geoapifyService.getNearbyPlaces(lat, lon, radiusInMeters);

        if (data == null || data.getFeatures() == null) {
            return new ArrayList<>();
        }

        List<CompletableFuture<AttractionDto>> futures = new ArrayList<>();

        for (GeoapifyResponseDto.Feature feature : data.getFeatures()) {
            GeoapifyResponseDto.Properties props = feature.getProperties();

            if (props == null
                    || props.getName() == null
                    || props.getName().isBlank()
                    || props.getLat() == null
                    || props.getLon() == null) {
                continue;
            }

            futures.add(CompletableFuture.supplyAsync(() -> mapCached(props), executor));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
    }

    // Кэш: повторный запрос не ходит в Wikipedia заново
    private AttractionDto mapCached(GeoapifyResponseDto.Properties props) {
        String key = props.getName() + "_" + props.getLat() + "_" + props.getLon();

        AttractionDto cached = placeCache.get(key);
        if (cached != null) {
            return cached;
        }

        try {
            AttractionDto place = mapGeoapifyPlace(props);
            if (place != null) {
                placeCache.put(key, place);
            }
            return place;
        } catch (Exception e) {
            return null; // одно упавшее место не ломает весь список
        }
    }

    // Преобразование места из Geoapify
    private AttractionDto mapGeoapifyPlace(
            GeoapifyResponseDto.Properties props
    ) {
        String wikiTag = extractWikiTag(props);

        String searchTitle =
                wikiTag != null && !wikiTag.isBlank()
                        ? wikiTag
                        : props.getName();

        Map<String, String> wikiData =
                wikipediaService.getWikiData(searchTitle);

        String description = wikiData.get("description");

        // Отсекаем места без описания Wikipedia
        if (description == null || description.isBlank()) {
            return null;
        }

        String wikipediaUrl = props.getWiki();

        if (wikipediaUrl == null || wikipediaUrl.isBlank()) {
            wikipediaUrl = wikiData.get("wikiUrl");
        }

        String category =
                props.getCategories() != null
                        && !props.getCategories().isEmpty()
                        ? props.getCategories().get(0)
                        : "attraction";

        return AttractionDto.builder()
                .title(props.getName())
                .description(description)
                .address(props.getAddress())
                .latitude(props.getLat())
                .longitude(props.getLon())
                .category(category)
                .imageUrl(wikiData.get("imageUrl"))
                .websiteUrl(props.getWebsite())
                .wikipediaUrl(wikipediaUrl)
                .build();
    }

    // Получение Wikipedia-тега
    private String extractWikiTag(
            GeoapifyResponseDto.Properties props
    ) {
        if (props.getDatasource() != null
                && props.getDatasource().getRaw() != null) {

            return props.getDatasource().getRaw().getWikipedia();
        }

        return props.getWiki();
    }

    // Получение мест из MySQL
    public List<AttractionDto> getNearbyPlacesFromDatabase(
            double lat,
            double lon,
            double radiusMeters
    ) {
        if (radiusMeters < 0 || radiusMeters > 20000) {
            return Collections.emptyList();
        }

        List<DataEntity> allPlaces = dataRepository.findAll();

        Map<String, DataEntity> uniquePlaces = new LinkedHashMap<>();

        allPlaces.stream()
                .filter(place ->
                        place.getName() != null
                                && place.getLatitude() != null
                                && place.getLongitude() != null
                )
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
                .map(this::mapDatabasePlace)
                .toList();
    }

    // Преобразование места из MySQL
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

    // Объединение MySQL и Geoapify
    public List<AttractionDto> getAllNearbyPlaces(
            double lat,
            double lon,
            double radiusMeters
    ) {
        List<AttractionDto> combinedList = new ArrayList<>();

        // Сначала добавляем места из MySQL
        combinedList.addAll(
                getNearbyPlacesFromDatabase(lat, lon, radiusMeters)
        );

        // Затем добавляем места из Geoapify
        combinedList.addAll(
                getNearbyPlaces(lat, lon, radiusMeters)
        );

        // Удаляем дубликаты
        List<AttractionDto> uniquePlaces = new ArrayList<>();

        for (AttractionDto place : combinedList) {

            boolean duplicate = uniquePlaces.stream().anyMatch(existing -> {

                if (existing.getLatitude() == null
                        || existing.getLongitude() == null
                        || place.getLatitude() == null
                        || place.getLongitude() == null
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

    // Получение подробностей по координатам из обоих источников
    public AttractionDto getPlaceByCoordinates(
            Double lat,
            Double lon
    ) {
        if (lat == null || lon == null) {
            return null;
        }

        List<AttractionDto> places =
                getAllNearbyPlaces(lat, lon, 50);

        return places.stream()
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
                        ) <= 20
                )
                .min(Comparator.comparingDouble(place ->
                        GeoUtils.calculateDistanceInMeters(
                                lat,
                                lon,
                                place.getLatitude(),
                                place.getLongitude()
                        )
                ))
                .orElse(null);
    }

    // Поиск достопримечательностей через Geoapify
    public List<AttractionDto> searchPlaces(String query) {

        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        GeoapifyResponseDto data =
                geoapifyService.searchPlaces(query.trim());

        if (data == null || data.getFeatures() == null) {
            return new ArrayList<>();
        }

        List<CompletableFuture<AttractionDto>> futures = new ArrayList<>();

        for (GeoapifyResponseDto.Feature feature : data.getFeatures()) {
            GeoapifyResponseDto.Properties props = feature.getProperties();

            if (props == null
                    || props.getName() == null
                    || props.getName().isBlank()
                    || props.getLat() == null
                    || props.getLon() == null) {
                continue;
            }

            futures.add(CompletableFuture.supplyAsync(() -> mapCached(props), executor));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
    }
}