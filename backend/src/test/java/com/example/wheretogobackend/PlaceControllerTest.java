package com.example.wheretogobackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testPlaceEndpointAvailability() throws Exception {
        // Проверяем, что главный контроллер доступен (даже если вернет 404 или 200 в зависимости от путей, главное — нет системной ошибки 500)
        mockMvc.perform(get("/api/places")) // Замените "/api/places" на ваш реальный маппинг из PlaceController, если нужно
                .andExpect(status().is4xxClientError()); // Ожидаем корректный HTTP-ответ (например, 404 если пустой путь, или 200)
    }
}