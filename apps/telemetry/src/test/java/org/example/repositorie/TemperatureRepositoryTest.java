package org.example.repositorie;

import org.example.model.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TemperatureRepositoryTest {

    @Autowired
    private TemperatureRepository repository;

    @Test
    @DisplayName("save() должен сохранять запись температуры и проставлять id")
    void save_ShouldPersistEntity() {
        Temperature t = new Temperature();
        t.setSensorId("sensor-1");
        t.setValue(23.5);
        t.setUnit("Celsius");
        t.setTimestamp(Instant.now());
        t.setLocation("kitchen");
        t.setStatus("ok");
        t.setSensorType("temperature");
        t.setDescription("initial record");

        Temperature saved = repository.save(t);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.count()).isEqualTo(1);
        Temperature found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getSensorId()).isEqualTo("sensor-1");
        assertThat(found.getValue()).isEqualTo(23.5);
        assertThat(found.getUnit()).isEqualTo("Celsius");
        assertThat(found.getLocation()).isEqualTo("kitchen");
        assertThat(found.getStatus()).isEqualTo("ok");
        assertThat(found.getSensorType()).isEqualTo("temperature");
        assertThat(found.getDescription()).isEqualTo("initial record");
    }
}


