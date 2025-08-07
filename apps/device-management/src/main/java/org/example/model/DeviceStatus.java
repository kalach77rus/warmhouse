package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatus {
    @JsonProperty("device_id")
    private String deviceId;
    private String status;
    @JsonProperty("last_seen")
    private LocalDateTime lastSeen;
    private String message;
}
