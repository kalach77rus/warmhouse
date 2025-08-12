package org.example.telemetry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class TemperatureClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TemperatureClient(RestTemplate restTemplate,
                             @Value("${temperature.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Map<String, Object> getTemperatureBySensorId(String sensorId) {
        String url = baseUrl + "/temperature/" + sensorId;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        return response.getBody();
    }
}


