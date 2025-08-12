package org.example.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.TemperatureService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemperaturePoller {

    private final TemperatureService temperatureService;

    @Scheduled(fixedDelayString = "${telemetry.poll-interval-ms:5000}")
    public void pollAndSave() {
        temperatureService.pollAndSave();
    }
}


