package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TemperatureApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Проверяем, что Spring контекст загружается корректно
    }

    @Test
    void getTemperature_IntegrationTest() throws Exception {

        mockMvc.perform(get("/temperature")
                .param("location", "Bedroom"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.value").exists())
                .andExpect(jsonPath("$.unit").value("Celsius"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.location").value("Bedroom"))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.sensor_id").exists())
                .andExpect(jsonPath("$.sensor_type").value("temperature"))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void getTemperature_MultipleRequests_ShouldReturnDifferentTemperatures() throws Exception {

        // Первый запрос
        String response1 = mockMvc.perform(get("/temperature")
                .param("location", "Moscow"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Второй запрос
        String response2 = mockMvc.perform(get("/temperature")
                .param("location", "Moscow"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Проверяем, что ответы разные (температура генерируется случайно)
        org.junit.jupiter.api.Assertions.assertNotEquals(response1, response2);
    }
} 