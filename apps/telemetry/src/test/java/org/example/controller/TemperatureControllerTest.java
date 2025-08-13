package org.example.controller;

import org.example.model.Temperature;
import org.example.repositorie.TemperatureRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TemperatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TemperatureRepository repository;

    @Test
    @DisplayName("GET /api/v1/temperatures возвращает список DTO (через TelemetryController)")
    void getAll_ShouldReturnList_TelemetryController() throws Exception {
        Temperature t = new Temperature();
        t.setSensorId("sensor-x");
        t.setValue(22.2);
        t.setUnit("Celsius");
        t.setTimestamp(Instant.now());
        t.setLocation("balcony");
        t.setStatus("ok");
        t.setSensorType("temperature");
        repository.save(t);

        mockMvc.perform(get("/api/v1/temperatures").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].sensorId", notNullValue()))
                .andExpect(jsonPath("$[0].value", notNullValue()))
                .andExpect(jsonPath("$[0].unit", notNullValue()))
                .andExpect(jsonPath("$[0].timestamp", notNullValue()))
                .andExpect(jsonPath("$[0].location", notNullValue()))
                .andExpect(jsonPath("$[0].status", notNullValue()))
                .andExpect(jsonPath("$[0].sensorType", notNullValue()));
    }
}


