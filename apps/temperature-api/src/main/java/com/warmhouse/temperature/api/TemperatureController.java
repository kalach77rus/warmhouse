package com.warmhouse.temperature.api;

import com.warmhouse.temperature.model.HeatCommand;
import com.warmhouse.temperature.model.HeatState;
import com.warmhouse.temperature.service.HeatControlService;
import com.warmhouse.temperature.service.TemperatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TemperatureController {

    private final TemperatureService temperatureService;
    
    @Autowired
    private HeatControlService heatControlService;

    public TemperatureController(TemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    @GetMapping("/temperature")
    public ResponseEntity<Map<String, Object>> getTemperature(@RequestParam(name = "location", required = false, defaultValue = "default") String location) {
        double valueCelsius = temperatureService.getTemperature(location);
        Map<String, Object> body = new HashMap<>();
        body.put("location", location);
        body.put("value", valueCelsius);
        body.put("unit", "C");
        return ResponseEntity.ok(body);
    }
    
    @PostMapping("/commands")
    public ResponseEntity<Map<String, Object>> processCommand(@RequestBody HeatCommand command) {
        System.out.println("=== TEMPERATURE API RECEIVED COMMAND ===");
        System.out.println("Command type: " + command.getType());
        System.out.println("Device ID: " + command.getDeviceId());
        System.out.println("Command data: " + command.getData());
        
        heatControlService.processCommand(command.getDeviceId(), command.getType(), command.getData());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Command processed successfully");
        response.put("deviceId", command.getDeviceId());
        response.put("commandType", command.getType());
        
        System.out.println("Command processed successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/heat/state/{deviceId}")
    public ResponseEntity<HeatState> getHeatState(@PathVariable String deviceId) {
        HeatState state = heatControlService.getHeatState(deviceId);
        return ResponseEntity.ok(state);
    }
}


