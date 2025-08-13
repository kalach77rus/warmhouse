package org.example.service;

import org.example.dto.TemperatureDto;
import org.example.model.Temperature;

import java.util.List;

public interface TemperatureService {

    long count();

    Temperature save(Temperature temperature);

    void pollAndSave();

    TemperatureDto getFirstByLocation(String location);

    TemperatureDto getFirstBySensorId(String sensorId);

    List<TemperatureDto> getAll();
}


