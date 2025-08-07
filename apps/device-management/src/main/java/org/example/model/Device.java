package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private String id;
    private String name;
    private String type;
    private String location;
    private String status;
    private String manufacturer;
    private String model;
    @JsonProperty("firmware_version")
    private String firmwareVersion;
    @JsonProperty("ip_address")
    private String ipAddress;
    @JsonProperty("mac_address")
    private String macAddress;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
