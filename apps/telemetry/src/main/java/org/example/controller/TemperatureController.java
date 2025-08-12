package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.TemperatureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TemperatureController {

    private final TemperatureService temperatureService;

    @GetMapping("/api/v1/count")
    public long count() {
        return temperatureService.count();
    }
}
