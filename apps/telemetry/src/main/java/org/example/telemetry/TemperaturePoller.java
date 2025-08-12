package org.example.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Random;

@Component
public class TemperaturePoller {

    private static final Logger log = LoggerFactory.getLogger(TemperaturePoller.class);

    private final TemperatureClient temperatureClient;
    private final TemperatureLogRepository repository;
    private final Random random = new Random();

    public TemperaturePoller(TemperatureClient temperatureClient, TemperatureLogRepository repository) {
        this.temperatureClient = temperatureClient;
        this.repository = repository;
    }

    @Scheduled(fixedDelayString = "${telemetry.poll-interval-ms:5000}")
    public void pollAndSave() {
        String sensorId = String.valueOf(random.nextInt(3) + 1);
        try {
            Map<String, Object> payload = temperatureClient.getTemperatureBySensorId(sensorId);
            if (payload == null) {
                log.warn("Empty response for sensor {}", sensorId);
                return;
            }

            TemperatureLog logEntity = new TemperatureLog();
            logEntity.setSensorId(String.valueOf(payload.getOrDefault("sensor_id", sensorId)));
            Object value = payload.get("value");
            if (!(value instanceof Number number)) {
                log.warn("Unexpected 'value' type: {} for sensor {}", value == null ? "null" : value.getClass().getName(), sensorId);
                return;
            }
            logEntity.setValue(number.doubleValue());
            logEntity.setUnit(String.valueOf(payload.getOrDefault("unit", "Celsius")));
            Object ts = payload.get("timestamp");
            Instant timestamp = ts != null ? Instant.parse(String.valueOf(ts)) : Instant.now();
            logEntity.setTimestamp(timestamp);
            logEntity.setLocation(String.valueOf(payload.getOrDefault("location", "Unknown")));
            logEntity.setStatus(String.valueOf(payload.getOrDefault("status", "unknown")));
            logEntity.setSensorType(String.valueOf(payload.getOrDefault("sensor_type", "temperature")));
            Object desc = payload.get("description");
            logEntity.setDescription(desc != null ? String.valueOf(desc) : null);

            repository.save(logEntity);
            log.info("Saved temperature log for sensor {} value {} {}", logEntity.getSensorId(), logEntity.getValue(), logEntity.getUnit());
        } catch (Exception e) {
            log.error("Failed to poll temperature for sensor {}", sensorId, e);
        }
    }
}


