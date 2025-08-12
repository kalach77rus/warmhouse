package org.example.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TemperatureClient {

    private final RestClient restClient;

    @Value("${temperature.api.base-url}")
    private String baseUrl;

    public Map<String, Object> getTemperatureBySensorId(String sensorId) {
        String url = baseUrl + "/temperature/" + sensorId;
        ResponseEntity<Map<String, Object>> response = restClient
                .get()
                .uri(url)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
        return response.getBody();
    }
}
