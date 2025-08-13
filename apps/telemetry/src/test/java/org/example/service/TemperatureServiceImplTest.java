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
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    @DisplayName("getFirstByLocation() возвращает DTO при существующей записи")
    void getFirstByLocation_ShouldReturnDto_WhenExists() {
        Temperature t = new Temperature();
        t.setSensorId("sensor-l-1");
        t.setValue(21.5);
        t.setUnit("Celsius");
        t.setTimestamp(Instant.now());
        t.setLocation("office");
        t.setStatus("ok");
        t.setSensorType("temperature");
        t.setDescription("desk sensor");
        repository.save(t);

        var dto = temperatureService.getFirstByLocation("office");

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getSensorId()).isEqualTo("sensor-l-1");
        assertThat(dto.getValue()).isEqualTo(21.5);
        assertThat(dto.getTimestamp()).isNotNull();
        assertThat(dto.getLocation()).isEqualTo("office");
        assertThat(dto.getStatus()).isEqualTo("ok");
        assertThat(dto.getSensorType()).isEqualTo("temperature");
        assertThat(dto.getDescription()).isEqualTo("desk sensor");
    }

    @Test
    @DisplayName("getFirstByLocation() кидает NotFound при отсутствии записи")
    void getFirstByLocation_ShouldThrowNotFound_WhenMissing() {
        assertThrows(org.example.exceptions.NotFoundException.class,
                () -> temperatureService.getFirstByLocation("missing-location"));
    }

    @Test
    @DisplayName("getFirstBySensorId() возвращает DTO при существующей записи")
    void getFirstBySensorId_ShouldReturnDto_WhenExists() {
        Temperature t = new Temperature();
        t.setSensorId("sensor-77");
        t.setValue(18.9);
        t.setUnit("Celsius");
        t.setTimestamp(Instant.now());
        t.setLocation("garage");
        t.setStatus("ok");
        t.setSensorType("temperature");
        t.setDescription("car area");
        repository.save(t);

        var dto = temperatureService.getFirstBySensorId("sensor-77");

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getSensorId()).isEqualTo("sensor-77");
        assertThat(dto.getValue()).isEqualTo(18.9);
        assertThat(dto.getTimestamp()).isNotNull();
        assertThat(dto.getLocation()).isEqualTo("garage");
        assertThat(dto.getStatus()).isEqualTo("ok");
        assertThat(dto.getSensorType()).isEqualTo("temperature");
        assertThat(dto.getDescription()).isEqualTo("car area");
    }

    @Test
    @DisplayName("getFirstBySensorId() кидает NotFound при отсутствии записи")
    void getFirstBySensorId_ShouldThrowNotFound_WhenMissing() {
        assertThrows(org.example.exceptions.NotFoundException.class,
                () -> temperatureService.getFirstBySensorId("unknown-sensor"));
    }

    @Test
    @DisplayName("pollAndSave() не сохраняет запись при null-ответе клиента")
    void pollAndSave_ShouldNotPersist_WhenClientReturnsNull() {
        given(temperatureClient.getTemperatureBySensorId(anyString())).willReturn(null);

        long before = repository.count();
        temperatureService.pollAndSave();
        long after = repository.count();

        assertThat(after).isEqualTo(before);
    }

    @Test
    @DisplayName("pollAndSave() не сохраняет запись при некорректном типе value")
    void pollAndSave_ShouldNotPersist_WhenValueHasWrongType() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sensor_id", "13");
        payload.put("value", "19.5"); // некорректный тип
        payload.put("unit", "Celsius");
        payload.put("timestamp", Instant.now().toString());
        payload.put("location", "attic");
        payload.put("status", "ok");
        payload.put("sensor_type", "temperature");
        payload.put("description", "bad value type");

        given(temperatureClient.getTemperatureBySensorId(anyString())).willReturn(payload);

        long before = repository.count();
        temperatureService.pollAndSave();
        long after = repository.count();

        assertThat(after).isEqualTo(before);
    }
}


