package com.example.wheretogobackend.repository;

import com.example.wheretogobackend.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {
}