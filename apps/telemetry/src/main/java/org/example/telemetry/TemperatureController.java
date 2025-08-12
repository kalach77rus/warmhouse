package org.example.telemetry;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TemperatureController {

    private final TemperatureLogRepository repository;

    public TemperatureController(TemperatureLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/logs/count")
    public long count() {
        return repository.count();
    }
}


