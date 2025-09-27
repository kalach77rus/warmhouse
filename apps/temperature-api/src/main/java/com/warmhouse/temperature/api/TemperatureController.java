package com.warmhouse.temperature.api;

import com.warmhouse.temperature.model.HeatCommand;
import com.warmhouse.temperature.model.HeatState;
import com.warmhouse.temperature.service.HeatControlService;
import com.warmhouse.temperature.service.TemperatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class TemperatureController {

    private final TemperatureService temperatureService;
    
    @Autowired
    private HeatControlService heatControlService;

    public TemperatureController(TemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    @GetMapping("/temperature")
    public ResponseEntity<Map<String, Object>> getTemperature(
            @RequestParam(name = "location", required = false, defaultValue = "default") String location,
            HttpServletRequest request) {
        
        log.info("=== TEMPERATURE API REQUEST ===");
        log.info("Location: {}", location);
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Query String: {}", request.getQueryString());
        log.info("Request Method: {}", request.getMethod());
        
        // Логируем все входящие заголовки
        log.info("Incoming Headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            log.info("  {}: {}", headerName, headerValue);
        }
        
        double valueCelsius = temperatureService.getTemperature(location);
        Map<String, Object> body = new HashMap<>();
        body.put("location", location);
        body.put("value", valueCelsius);
        body.put("unit", "C");
        
        log.info("Temperature response: {}", body);
        log.info("=== END TEMPERATURE API REQUEST ===");
        
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


