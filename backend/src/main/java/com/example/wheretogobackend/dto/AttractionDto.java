package com.example.wheretogobackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttractionDto {
    private String title;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private String category;
    private String imageUrl;
    private String websiteUrl;
    private String wikipediaUrl;
}