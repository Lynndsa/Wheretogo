package com.example.wheretogobackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "attractions")
@Data
public class Attraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String address;

    private Double latitude;

    private Double longitude;

    private String category;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "website_url", columnDefinition = "TEXT")
    private String websiteUrl;

    @Column(name = "wikipedia_url", columnDefinition = "TEXT")
    private String wikipediaUrl;
}