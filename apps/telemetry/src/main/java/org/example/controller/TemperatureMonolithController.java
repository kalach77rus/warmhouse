package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorResponse;
import org.example.dto.TemperatureDto;
import org.example.service.TemperatureService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Telemetry (Monolith)", description = "Эндпоинты чтения телеметрии температуры (для монолита)")
public class TemperatureMonolithController {

    private final TemperatureService service;

    @Operation(summary = "Получить телеметрию по локации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Неверные параметры",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/temperature")
    public Map<String, Object> getTemperature(
            @Parameter(description = "Локация датчика", required = true)
            @RequestParam(required = true) String location) {

        log.info("Запрос получения телеметрии по location={}",location);
        var dto = service.getFirstByLocation(location);
        log.info("Телеметрия по location={}",dto);
        return convertTemperature(dto);
    }

    @Operation(summary = "Получить телеметрию по sensorId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Неверные параметры",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/temperature/{sensorId}")
    public Map<String, Object> getTemperatureBySensorId(
            @Parameter(description = "Идентификатор датчика", required = true)
            @PathVariable String sensorId) {

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
