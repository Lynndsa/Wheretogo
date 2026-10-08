package com.example.wheretogobackend.repository;

import com.example.wheretogobackend.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    // 1. Поиск по точному совпадению названия или Wikipedia URL (полезно при парсинге, чтобы не дублировать локации)
    boolean existsByTitle(String title);

    Optional<Attraction> findByWikipediaUrl(String wikipediaUrl);

    // 2. Поиск по категории (например, "museum", "park", "historic")
    List<Attraction> findByCategoryIgnoreCase(String category);

    // 3. Поиск по части названия (регистронезависимый)
    List<Attraction> findByTitleContainingIgnoreCase(String title);

    // 4. Поиск мест в определенном радиусе от заданной точки (в километрах)
    // Использует формулу гаверсинусов (Haversine formula) для расчёта расстояния на сфере
    @Query(value = "SELECT *, " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * " +
            "cos(radians(a.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(a.latitude)))) AS distance " +
            "FROM attractions a " +
            "HAVING distance <= :radiusInKm " +
            "ORDER BY distance ASC",
            nativeQuery = true)
    List<Attraction> findNearbyAttractions(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusInKm") Double radiusInKm
    );
}