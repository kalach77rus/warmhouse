package org.example.controller;

import org.example.model.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@Tag(name = "Health", description = "Проверка работоспособности сервиса")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health-check", description = "Проверка статуса сервиса")
    public HealthResponse health() {
        log.info("Health check запрос получен");
        HealthResponse response = new HealthResponse("ok", "device-management");
        log.info("Health check ответ: status={}, service={}", response.getStatus(), response.getService());
        return response;
    }
}
