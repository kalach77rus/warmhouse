package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemperatureDto {

    private Long id;

    private String sensorId;

    private Double value;

    private String unit;

    private Instant timestamp;

    private String location;

    private String status;

    private String sensorType;

    private String description;
}
