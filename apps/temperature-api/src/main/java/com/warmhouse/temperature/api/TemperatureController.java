package com.warmhouse.temperature.api;

import com.warmhouse.temperature.service.TemperatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TemperatureController {

    private final TemperatureService temperatureService;

    public TemperatureController(TemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    @GetMapping("/temperature")
    public ResponseEntity<Map<String, Object>> getTemperature(@RequestParam(name = "location", required = false, defaultValue = "default") String location) {
        double valueCelsius = temperatureService.getTemperature(location);
        Map<String, Object> body = new HashMap<>();
        body.put("location", location);
        body.put("value", valueCelsius);
        body.put("unit", "C");
        return ResponseEntity.ok(body);
    }
}


