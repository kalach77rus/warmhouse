package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.TemperatureClient;
import org.example.model.Temperature;
import org.example.repositorie.TemperatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemperatureServiceImpl implements TemperatureService {

    private final TemperatureRepository repository;
    private final TemperatureClient temperatureClient;
    private final PlatformTransactionManager transactionManager;

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    @Override
    @Transactional
    public Temperature save(Temperature temperature) {
        return repository.save(temperature);
    }

    @Override
    public void pollAndSave() {
        String sensorId = String.valueOf((int) (Math.random() * 3) + 1);
        try {
            var payload = temperatureClient.getTemperatureBySensorId(sensorId);
            if (payload == null) {
                log.warn("Empty response for sensor {}", sensorId);
                return;
            }

            Temperature temperature = new Temperature();
            temperature.setSensorId(String.valueOf(payload.getOrDefault("sensor_id", sensorId)));
            Object value = payload.get("value");
            if (!(value instanceof Number number)) {
                log.warn("Unexpected 'value' type: {} for sensor {}", value == null ? "null" : value.getClass().getName(), sensorId);
                return;
            }
            temperature.setValue(number.doubleValue());
            temperature.setUnit(String.valueOf(payload.getOrDefault("unit", "Celsius")));
            Object ts = payload.get("timestamp");
            var timestamp = ts != null ? java.time.Instant.parse(String.valueOf(ts)) : java.time.Instant.now();
            temperature.setTimestamp(timestamp);
            temperature.setLocation(String.valueOf(payload.getOrDefault("location", "Unknown")));
            temperature.setStatus(String.valueOf(payload.getOrDefault("status", "unknown")));
            temperature.setSensorType(String.valueOf(payload.getOrDefault("sensor_type", "temperature")));
            Object desc = payload.get("description");
            temperature.setDescription(desc != null ? String.valueOf(desc) : null);

            TransactionTemplate template = new TransactionTemplate(transactionManager);
            Temperature saved = template.execute(status -> repository.save(temperature));
            if (saved != null) {
                log.info("Saved temperature log for sensor {} value {} {}", saved.getSensorId(), saved.getValue(), saved.getUnit());
            } else {
                log.warn("Temperature save returned null for sensor {}", temperature.getSensorId());
            }
        } catch (Exception e) {
            log.error("Failed to poll temperature for sensor {}", sensorId, e);
        }
    }
}


