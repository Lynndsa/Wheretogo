package com.example.wheretogobackend.service;

import com.example.wheretogobackend.dto.AttractionDto;
import com.example.wheretogobackend.dto.GeoapifyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final GeoapifyService geoapifyService;
    private final WikipediaService wikipediaService;

    public List<AttractionDto> getNearbyPlaces(Double lat, Double lon, Double radiusInMeters) {
        GeoapifyResponseDto geoapifyData = geoapifyService.getNearbyPlaces(lat, lon, radiusInMeters);
        List<AttractionDto> attractions = new ArrayList<>();

        if (geoapifyData == null || geoapifyData.getFeatures() == null) {
            return attractions;
        }

        for (GeoapifyResponseDto.Feature feature : geoapifyData.getFeatures()) {
            GeoapifyResponseDto.Properties props = feature.getProperties();
            if (props == null || props.getName() == null || props.getName().isBlank()) {
                continue;
            }

            String title = props.getName();

            // Игнорируем слишком короткие названия на латинице (статуи/экспонаты типа "Rubens", "Smilis")
            if (isLatinOnly(title)) {
                continue;
            }

            Double itemLat = props.getLat();
            Double itemLon = props.getLon();

            String category = (props.getCategories() != null && !props.getCategories().isEmpty())
                    ? props.getCategories().get(0)
                    : "attraction";

            String imageUrl = null;
            String description = null;
            String wikipediaUrl = props.getWiki();

            // 1. Сначала ищем по точному тегу Википедии из Geoapify
            String wikiTag = extractWikiTag(props);

            if (wikiTag != null && !wikiTag.isEmpty()) {
                Map<String, String> wikiData = wikipediaService.getWikiData(wikiTag);
                description = wikiData.get("description");
                imageUrl = wikiData.get("imageUrl");
                if (wikipediaUrl == null) {
                    wikipediaUrl = wikiData.get("wikiUrl");
                }
            }

            // 2. ФОЛЛБЭК: Пробуем искать по названию, ТОЛЬКО если оно написано по-русски (на кириллице)
            if (description == null && isCyrillic(title)) {
                Map<String, String> fallbackWikiData = wikipediaService.getWikiData("ru:" + title);
                if (fallbackWikiData.containsKey("description")) {
                    description = fallbackWikiData.get("description");
                    imageUrl = fallbackWikiData.get("imageUrl");
                    wikipediaUrl = fallbackWikiData.get("wikiUrl");
                }
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

    private String extractWikiTag(GeoapifyResponseDto.Properties props) {
        if (props.getDatasource() != null && props.getDatasource().getRaw() != null) {
            return props.getDatasource().getRaw().getWikipedia();
        }
        return props.getWiki();
    }

    private boolean isCyrillic(String text) {
        return text.matches(".*[а-яА-ЯёЁ].*");
    }

    private boolean isLatinOnly(String text) {
        return text.matches("^[a-zA-Z0-9\\s\\.\\-]+$");
    }
}