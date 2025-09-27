package com.warmhouse.temperature.service;

import com.warmhouse.temperature.model.HeatState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HeatControlService {
    
    // Простое хранение состояния отопления в памяти
    private final Map<String, HeatState> heatStates = new ConcurrentHashMap<>();
    
    public HeatState getHeatState(String deviceId) {
        HeatState state = heatStates.computeIfAbsent(deviceId, id -> {
            HeatState newState = new HeatState();
            newState.setDeviceId(id);
            newState.setCurrentTemperature(20.0);
            newState.setTargetTemperature(22.0);
            newState.setMode("AUTO");
            newState.setHeatingEnabled(false);
            newState.setStatus("ACTIVE");
            return newState;
        });
        
        System.out.println("=== GETTING HEAT STATE ===");
        System.out.println("Device ID: " + deviceId);
        System.out.println("Current state: " + state);
        return state;
    }
    
    public void setMode(String deviceId, String mode) {
        HeatState state = getHeatState(deviceId);
        System.out.println("=== SETTING HEAT MODE ===");
        System.out.println("Device ID: " + deviceId);
        System.out.println("New mode: " + mode);
        System.out.println("Previous mode: " + state.getMode());
        
        state.setMode(mode);
        
        if ("OFF".equals(mode)) {
            state.setHeatingEnabled(false);
            state.setStatus("INACTIVE");
        } else {
            state.setStatus("ACTIVE");
        }
        
        System.out.println("Heat mode set to " + mode + " for device " + deviceId);
        System.out.println("Current state: " + state);
    }
    
    public void setTargetTemperature(String deviceId, double temperature) {
        HeatState state = getHeatState(deviceId);
        state.setTargetTemperature(temperature);
        
        // Простая логика: если текущая температура ниже целевой, включаем отопление
        if (state.getCurrentTemperature() < temperature) {
            state.setHeatingEnabled(true);
            state.setStatus("ACTIVE");
        } else {
            state.setHeatingEnabled(false);
            state.setStatus("INACTIVE");
        }
        
        System.out.println("Target temperature set to " + temperature + "°C for device " + deviceId);
    }
    
    public void processCommand(String deviceId, String commandType, Object commandData) {
        System.out.println("Processing command " + commandType + " for device " + deviceId);
        
        if ("SET_MODE".equals(commandType)) {
            Map<String, Object> data = (Map<String, Object>) commandData;
            String mode = (String) data.get("mode");
            setMode(deviceId, mode);
        } else if ("SET_TEMPERATURE".equals(commandType)) {
            Map<String, Object> data = (Map<String, Object>) commandData;
            double temperature = ((Number) data.get("temperature")).doubleValue();
            setTargetTemperature(deviceId, temperature);
        }
    }
}
