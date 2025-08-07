package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUpdate {
    private String name;
    private String type;
    private String location;
    private String manufacturer;
    private String model;
    @JsonProperty("ip_address")
    private String ipAddress;
    @JsonProperty("mac_address")
    private String macAddress;
}
