package org.example.controller;

import org.example.model.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        log.info("Health check запрос получен");
        HealthResponse response = new HealthResponse("ok", "device-management");
        log.info("Health check ответ: status={}, service={}", response.getStatus(), response.getService());
        return response;
    }
}
