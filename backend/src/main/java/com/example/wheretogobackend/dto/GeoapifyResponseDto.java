package com.example.wheretogobackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoapifyResponseDto {
    private List<Feature> features;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private Properties properties;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private String name;
        @JsonProperty("formatted")
        private String address;
        private Double lat;
        private Double lon;
        private List<String> categories;
        private String website;
        private String wiki; // Geoapify часто сразу дает тег/ссылку на Википедию
        private Datasource datasource;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Datasource {
        private Raw raw;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Raw {
        private String wikipedia;
    }
}