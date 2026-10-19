package com.example.wheretogobackend;

import com.example.wheretogobackend.service.CustomPlaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CustomPlaceServiceTest {

    @Autowired(required = false)
    private CustomPlaceService customPlaceService;

    @Test
    public void testCustomPlaceServiceLoads() {
        // Проверяем, что сервис успешно создан и готов обрабатывать данные пользователя
        assertThat(customPlaceService).isNotNull();
    }
}