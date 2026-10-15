package com.example.wheretogobackend.mysql.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "custom_places")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_custom_place")
    private Long idCustomPlace;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String address;

    private Double latitude;

    private Double longitude;

    private String category;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "website_url", length = 1000)
    private String websiteUrl;

    @Column(name = "id_attraction")
    private Integer idAttraction;
}