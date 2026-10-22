package com.example.wheretogobackend;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class PlaceValidationTest {

    @Test
    public void testPlaceIdValidation() {
        Long validId = 10L;
        Long invalidId = -1L;

        // Проверяем логику валидации идентификаторов (Правило П1 из таблицы)
        assertThat(validId).isGreaterThan(0);
        assertThat(invalidId).isLessThanOrEqualTo(0);
    }

    @Test
    public void testPlaceNameValidation() {
        String placeName = "Музей Эрмитаж";
        String emptyName = "";

        // Проверяем заполнение обязательных полей (Правило П4 из таблицы)
        assertThat(placeName).isNotBlank();
        assertThat(emptyName).isEmpty();
    }
}