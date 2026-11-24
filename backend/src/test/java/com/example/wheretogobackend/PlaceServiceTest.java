package com.example.wheretogobackend;

import com.example.wheretogobackend.service.AttractionService;
import com.example.wheretogobackend.service.CustomPlaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PlaceServiceTest {

    @Autowired(required = false)
    private AttractionService attractionService;

    @Autowired(required = false)
    private CustomPlaceService customPlaceService;

    @Test
    void testImplementationsAndSourcesInteroperability() {
        assertThat(attractionService != null && customPlaceService != null)
                .isTrue();
    }
}