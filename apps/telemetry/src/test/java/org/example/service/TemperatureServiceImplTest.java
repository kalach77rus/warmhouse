package org.example.service;

import org.example.client.TemperatureClient;
import org.example.model.Temperature;
import org.example.repositorie.TemperatureRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TemperatureServiceImplTest {

    @Autowired
    private TemperatureService temperatureService;

    @Autowired
    private TemperatureRepository repository;

    @MockBean
    private TemperatureClient temperatureClient;

    @Test
    @DisplayName("pollAndSave() должен сохранять запись из клиента в БД")
    void pollAndSave_ShouldPersistClientPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sensor_id", "42");
        payload.put("value", 19.7);
        payload.put("unit", "Celsius");
        payload.put("timestamp", Instant.now().toString());
        payload.put("location", "living room");
        payload.put("status", "ok");
        payload.put("sensor_type", "temperature");
        payload.put("description", "from client");

        given(temperatureClient.getTemperatureBySensorId(anyString())).willReturn(payload);

        long before = repository.count();
        temperatureService.pollAndSave();
        long after = repository.count();

        assertThat(after).isEqualTo(before + 1);

        Temperature last = repository.findAll(Sort.by(Sort.Direction.DESC, "id")).get(0);
        assertThat(last.getSensorId()).isEqualTo("42");
        assertThat(last.getValue()).isEqualTo(19.7);
        assertThat(last.getUnit()).isEqualTo("Celsius");
        assertThat(last.getLocation()).isEqualTo("living room");
        assertThat(last.getStatus()).isEqualTo("ok");
        assertThat(last.getSensorType()).isEqualTo("temperature");
        assertThat(last.getDescription()).isEqualTo("from client");
    }

    @Test
    @DisplayName("count() возвращает количество записей")
    void count_ShouldReturnNumberOfRecords() {
        Temperature t = new Temperature();
        t.setSensorId("s-1");
        t.setValue(10.0);
        t.setUnit("Celsius");
        t.setTimestamp(Instant.now());
        t.setLocation("lab");
        t.setStatus("ok");
        t.setSensorType("temperature");
        t.setDescription(null);
        repository.save(t);

        assertThat(temperatureService.count()).isGreaterThanOrEqualTo(1);
    }
}


