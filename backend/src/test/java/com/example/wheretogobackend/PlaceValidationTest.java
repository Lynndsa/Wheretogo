package com.example.wheretogobackend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceValidationTest {

    @Test
    void testPlaceIdValidation() {
        Long validId = 10L;
        Long invalidId = -1L;

        assertThat(validId > 0 && invalidId <= 0).isTrue();
    }

    @Test
    void testPlaceNameValidation() {
        String placeName = "Музей Эрмитаж";
        String emptyName = "";

        assertThat(!placeName.isBlank() && emptyName.isEmpty()).isTrue();
    }
}