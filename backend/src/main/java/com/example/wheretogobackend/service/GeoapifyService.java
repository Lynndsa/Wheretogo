package com.example.wheretogobackend.service;

import com.example.wheretogobackend.dto.GeoapifyResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Locale;

@Service
public class GeoapifyService {

    private final WebClient webClient;
    private final String apiKey;

    public GeoapifyService(
            WebClient webClient,
            @Value("${geoapify.api.key:e3b529d1cb8a4184a469b672b7fd48d6}") String apiKey) {
        this.webClient = webClient;
        this.apiKey = apiKey;
    }

    public GeoapifyResponseDto getNearbyPlaces(Double lat, Double lon, Double radiusInMeters) {
        // Расширенный список категорий для достопримечательностей, музеев, парков и памятников
        String categories = "tourism.attraction,tourism.sights,building.historic,heritage,entertainment.museum,leisure.park";

        String filter = String.format(Locale.US, "circle:%f,%f,%f", lon, lat, radiusInMeters);
        String bias = String.format(Locale.US, "proximity:%f,%f", lon, lat);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.geoapify.com")
                        .path("/v2/places")
                        .queryParam("categories", categories)
                        .queryParam("filter", filter)
                        .queryParam("bias", bias)
                        .queryParam("limit", 100)
                        .queryParam("apiKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(GeoapifyResponseDto.class)
                .block();
    }
}