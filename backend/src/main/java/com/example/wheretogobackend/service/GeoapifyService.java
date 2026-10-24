
package com.example.wheretogobackend.service;

import com.example.wheretogobackend.dto.GeoapifyResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class GeoapifyService {

    private static final int PAGE_SIZE = 500;
    private static final int MAX_PAGES = 10;

    private final WebClient webClient;
    private final String apiKey;

    public GeoapifyService(
            WebClient webClient,
            @Value("${geoapify.api.key}") String apiKey
    ) {
        this.webClient = webClient;
        this.apiKey = apiKey;
    }

    public GeoapifyResponseDto getNearbyPlaces(
            Double lat,
            Double lon,
            Double radiusInMeters
    ) {
        String categories =
                "tourism.attraction,tourism.sights,building.historic,"
                        + "heritage,entertainment.museum,leisure.park";

        String filter = String.format(
                Locale.US,
                "circle:%f,%f,%f",
                lon,
                lat,
                radiusInMeters
        );

        String bias = String.format(
                Locale.US,
                "proximity:%f,%f",
                lon,
                lat
        );

        List<GeoapifyResponseDto.Feature> allFeatures =
                new ArrayList<>();

        GeoapifyResponseDto firstResponse = null;

        for (int page = 0; page < MAX_PAGES; page++) {
            int offset = page * PAGE_SIZE;

            try {
                GeoapifyResponseDto response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .scheme("https")
                                .host("api.geoapify.com")
                                .path("/v2/places")
                                .queryParam("categories", categories)
                                .queryParam("filter", filter)
                                .queryParam("bias", bias)
                                .queryParam("limit", PAGE_SIZE)
                                .queryParam("offset", offset)
                                .queryParam("apiKey", apiKey)
                                .build())
                        .retrieve()
                        .bodyToMono(GeoapifyResponseDto.class)
                        .block();

                if (response == null || response.getFeatures() == null) {
                    break;
                }

                if (firstResponse == null) {
                    firstResponse = response;
                }

                List<GeoapifyResponseDto.Feature> features =
                        response.getFeatures();

                allFeatures.addAll(features);

                System.out.printf(
                        "Geoapify: page=%d, offset=%d, received=%d, total=%d%n",
                        page + 1,
                        offset,
                        features.size(),
                        allFeatures.size()
                );

                // Неполная страница означает, что результаты закончились
                if (features.size() < PAGE_SIZE) {
                    break;
                }

            } catch (WebClientResponseException e) {
                System.err.printf(
                        "Geoapify HTTP %d при offset=%d: %s%n",
                        e.getStatusCode().value(),
                        offset,
                        e.getResponseBodyAsString()
                );

                throw e;

            } catch (Exception e) {
                System.err.println(
                        "Ошибка запроса Geoapify при offset="
                                + offset + ": " + e.getMessage()
                );

                throw e;
            }
        }

        if (firstResponse == null) {
            return null;
        }

        firstResponse.setFeatures(allFeatures);

        System.out.println(
                "Geoapify: всего получено объектов = "
                        + allFeatures.size()
        );

        return firstResponse;
    }
}
