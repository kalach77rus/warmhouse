package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Temperature", description = "DTO телеметрии температуры")
public class TemperatureDto {

    @Schema(description = "Идентификатор записи", example = "1")
    private Long id;

    @Schema(description = "Идентификатор датчика", example = "sensor-123")
    private String sensorId;

    @Schema(description = "Значение температуры", example = "23.5")
    private Double value;

    @Schema(description = "Единица измерения", example = "C")
    private String unit;

    @Schema(description = "Момент измерения в UTC", example = "2024-07-10T12:30:00Z")
    private Instant timestamp;

    @Schema(description = "Локация датчика", example = "kitchen")
    private String location;

    @Schema(description = "Статус датчика", example = "OK")
    private String status;

    @Schema(description = "Тип датчика", example = "thermometer")
    private String sensorType;

    @Schema(description = "Описание записи", example = "Temperature sensor sensor-123")
    private String description;
}
