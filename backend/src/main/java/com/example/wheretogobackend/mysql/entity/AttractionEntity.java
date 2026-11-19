package com.example.wheretogobackend.mysql.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attractions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttractionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_attraction")
    private Integer idAttraction;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;
}
