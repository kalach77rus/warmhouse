package org.example.mapper;

import org.example.dto.TemperatureDto;
import org.example.model.Temperature;
import org.springframework.stereotype.Component;

@Component
public class TemperatureMapper {

    public TemperatureDto toDto(Temperature entity) {
        TemperatureDto dto = new TemperatureDto();
        dto.setId(entity.getId());
        dto.setSensorId(entity.getSensorId());
        dto.setValue(entity.getValue());
        dto.setTimestamp(entity.getTimestamp());
        dto.setLocation(entity.getLocation());
        dto.setStatus(entity.getStatus());
        dto.setSensorType(entity.getSensorType());
        dto.setDescription(entity.getDescription());
        return  dto;
    }
}
