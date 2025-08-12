package org.example.service;

import org.example.model.Temperature;

public interface TemperatureService {

    long count();

    Temperature save(Temperature temperature);

    void pollAndSave();
}


