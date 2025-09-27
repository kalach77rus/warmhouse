package com.warmhouseyandex.heat_service.integration;

import com.warmhouseyandex.heat_service.model.HeatState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ModuleAdapter {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${gateway.url:http://modules-gateway:8083}")
    private String gatewayUrl;
    
    /**
     * Отправляет команду к устройству отопления через gateway
     */
    public void sendCommand(String deviceId, String commandType, Object commandData) {
        try {
            // Формируем URL для отправки команды к устройству через gateway
            String url = String.format("%s/api/v1/modules/%s/proxy/commands", gatewayUrl, deviceId);
            
            Map<String, Object> command = new HashMap<>();
            command.put("type", commandType);
            command.put("deviceId", deviceId);
            command.put("data", commandData);
            
            System.out.println("Sending command to device: " + commandType + " for device " + deviceId);
            
            // Отправляем команду через gateway
            restTemplate.postForObject(url, command, Map.class);
            
        } catch (Exception e) {
            System.err.println("Error sending command to device: " + e.getMessage());
        }
    }
    
    /**
     * Читает состояние от устройства отопления через gateway
     */
    public HeatState readState(String deviceId) {
        try {
            // Получаем данные о температуре от устройства через gateway
            String url = String.format("%s/api/v1/modules/%s/proxy/temperature?location=default", gatewayUrl, deviceId);
            
            System.out.println("Reading state from device " + deviceId);
            
            // Получаем данные о температуре
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null) {
                HeatState state = new HeatState();
                state.setDeviceId(deviceId);
                state.setCurrentTemperature(((Number) response.get("value")).doubleValue());
                state.setTargetTemperature(22.0); // По умолчанию
                state.setMode("AUTO");
                state.setHeatingEnabled(true);
                state.setStatus("ACTIVE");
                return state;
            }
            
        } catch (Exception e) {
            System.err.println("Error reading state from device: " + e.getMessage());
        }
        
        // Возвращаем состояние по умолчанию в случае ошибки
        HeatState defaultState = new HeatState();
        defaultState.setDeviceId(deviceId);
        defaultState.setCurrentTemperature(20.0);
        defaultState.setTargetTemperature(22.0);
        defaultState.setMode("AUTO");
        defaultState.setHeatingEnabled(false);
        defaultState.setStatus("ERROR");
        return defaultState;
    }
}
