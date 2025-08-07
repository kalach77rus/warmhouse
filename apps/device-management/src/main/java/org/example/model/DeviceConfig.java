package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceConfig {
    @JsonProperty("device_id")
    private String deviceId;
    private Map<String, Object> settings;
    @JsonProperty("firmware_version")
    private String firmwareVersion;
    @JsonProperty("update_available")
    private Boolean updateAvailable;
    @JsonProperty("last_updated")
    private LocalDateTime lastUpdated;
}
