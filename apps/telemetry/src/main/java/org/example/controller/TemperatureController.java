package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.TemperatureDto;
import org.example.model.Temperature;
import org.example.service.TemperatureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TemperatureController {

    private final TemperatureService service;

    @GetMapping("/count")
    public long count() {
        return service.count();
    }

    @GetMapping("/temperature")
    public Map<String, Object> getTemperature(@RequestParam(required = true) String location) {

        log.info("Запрос получения телеметрии по location={}",location);
        var dto = service.getFirstByLocation(location);
        log.info("Телеметрия по location={}",dto);
        return convertTemperature(dto);
    }

    @GetMapping("/temperature/{sensorId}")
    public Map<String, Object> getTemperatureBySensorId(@PathVariable String sensorId) {

        log.info("Запрос получения телеметрии по sensorId={}",sensorId);
        var dto = service.getFirstBySensorId(sensorId);
        log.info("Телеметрия по sensorId={}",dto);
        return convertTemperature(dto);
    }

    private Map<String, Object> convertTemperature(TemperatureDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("value", dto.getValue());
        map.put("unit", dto.getUnit());
        map.put("timestamp", dto.getTimestamp());
        map.put("location", dto.getLocation());
        map.put("status", dto.getStatus());
        map.put("sensor_id", dto.getSensorId());
        map.put("sensor_type", dto.getSensorType());
        map.put("description", "Temperature sensor " + dto.getSensorId());

        return map;
    }
}
