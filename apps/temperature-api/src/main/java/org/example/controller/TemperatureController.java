package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class TemperatureController {

    private final Random random = new Random();

    @GetMapping("/temperature")
    public Map<String, Object> getTemperature(@RequestParam(required = true) String location) {
        // Генерируем случайную температуру от -30 до +40 градусов
        double temperature = -30 + (70 * random.nextDouble());
        String sensorId = generateSensorId(location);

        Map<String, Object> response = new HashMap<>();
        response.put("value", Math.round(temperature * 100.0) / 100.0);
        response.put("unit", "Celsius");
        response.put("timestamp", java.time.Instant.now().toString());
        response.put("location", location);
        response.put("status", "active");
        response.put("sensor_id", sensorId);
        response.put("sensor_type", "temperature");
        response.put("description", "Temperature sensor " + sensorId);

        return response;
    }

    @GetMapping("/temperature/{sensorId}")
    public Map<String, Object> getTemperatureBySensorId(@PathVariable String sensorId) {
        // Генерируем случайную температуру от -30 до +40 градусов
        double temperature = -30 + (70 * random.nextDouble());

        Map<String, Object> response = new HashMap<>();
        response.put("value", Math.round(temperature * 100.0) / 100.0);
        response.put("unit", "Celsius");
        response.put("timestamp", java.time.Instant.now().toString());
        response.put("location", generateLocation(sensorId));
        response.put("status", "active");
        response.put("sensor_id", sensorId);
        response.put("sensor_type", "temperature");
        response.put("description", "Temperature sensor " + sensorId);

        return response;
    }

    private String generateSensorId(String location) {
        return switch (location) {
            case "Living Room" -> "1";
            case "Bedroom" -> "2";
            case "Kitchen" -> "3";
            default -> "0";
        };
    }

    private String generateLocation(String sensorId) {
        return switch (sensorId) {
            case "1" -> "Living Room";
            case "2" -> "Bedroom";
            case "3" -> "Kitchen";
            default -> "Unknown";
        };
    }
}
