package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.*;
import org.example.service.DeviceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceService deviceService;

    @Test
    @DisplayName("GET /api/v1/devices -> 200 OK with list")
    void getAllDevices_ok() throws Exception {
        Device d1 = new Device("1", "Thermostat", "sensor", "kitchen", "online", "Acme", "T1000", "1.0.0", "10.0.0.2", "AA:BB:CC", LocalDateTime.now().minusDays(1), LocalDateTime.now());
        Device d2 = new Device("2", "Camera", "camera", "yard", "offline", "Acme", "C200", "2.3.1", "10.0.0.3", "DD:EE:FF", LocalDateTime.now().minusDays(2), LocalDateTime.now());
        when(deviceService.getAllDevices(any(), any(), any())).thenReturn(List.of(d1, d2));

        mockMvc.perform(get("/api/v1/devices").param("type", "sensor"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Thermostat")))
                .andExpect(jsonPath("$[1].name", is("Camera")));
    }

    @Test
    @DisplayName("GET /api/v1/devices -> 500 on exception")
    void getAllDevices_error() throws Exception {
        when(deviceService.getAllDevices(any(), any(), any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("Internal server error")))
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")));
    }

    @Test
    @DisplayName("POST /api/v1/devices -> 201 Created")
    void createDevice_created() throws Exception {
        DeviceCreate request = new DeviceCreate("Thermostat", "sensor", "kitchen", "Acme", "T1000", "10.0.0.2", "AA:BB:CC");
        Device created = new Device("123", request.getName(), request.getType(), request.getLocation(), "online", request.getManufacturer(), request.getModel(), "1.0.0", request.getIpAddress(), request.getMacAddress(), LocalDateTime.now(), LocalDateTime.now());

        when(deviceService.createDevice(any(DeviceCreate.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("123")))
                .andExpect(jsonPath("$.name", is("Thermostat")));
    }

    @Test
    @DisplayName("POST /api/v1/devices -> 400 on bad request")
    void createDevice_badRequest() throws Exception {
        DeviceCreate request = new DeviceCreate(null, null, null, null, null, null, null);
        when(deviceService.createDevice(any(DeviceCreate.class))).thenThrow(new IllegalArgumentException("invalid"));

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("POST /api/v1/devices -> 500 on error")
    void createDevice_error() throws Exception {
        DeviceCreate request = new DeviceCreate("n", "t", "l", null, null, null, null);
        when(deviceService.createDevice(any(DeviceCreate.class))).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")));
    }

    @Test
    @DisplayName("GET /api/v1/devices/{id} -> 200 when found")
    void getDeviceById_found() throws Exception {
        Device device = new Device("42", "Thermostat", "sensor", "kitchen", "online", null, null, null, null, null, null, null);
        when(deviceService.getDeviceById(eq("42"))).thenReturn(Optional.of(device));

        mockMvc.perform(get("/api/v1/devices/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("42")));
    }

    @Test
    @DisplayName("GET /api/v1/devices/{id} -> 404 when not found")
    void getDeviceById_notFound() throws Exception {
        when(deviceService.getDeviceById(eq("404"))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/devices/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("DEVICE_NOT_FOUND")));
    }

    @Test
    @DisplayName("PUT /api/v1/devices/{id} -> 200 when updated")
    void updateDevice_ok() throws Exception {
        DeviceUpdate request = new DeviceUpdate("Thermostat-2", "sensor", "kitchen", null, null, null, null);
        Device updated = new Device("7", request.getName(), request.getType(), request.getLocation(), "online", null, null, null, null, null, null, null);
        when(deviceService.updateDevice(eq("7"), any(DeviceUpdate.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/v1/devices/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Thermostat-2")));
    }

    @Test
    @DisplayName("PUT /api/v1/devices/{id} -> 404 when not found")
    void updateDevice_notFound() throws Exception {
        when(deviceService.updateDevice(eq("8"), any(DeviceUpdate.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/devices/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceUpdate())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("DEVICE_NOT_FOUND")));
    }

    @Test
    @DisplayName("PUT /api/v1/devices/{id} -> 400 on bad data")
    void updateDevice_badRequest() throws Exception {
        when(deviceService.updateDevice(eq("9"), any(DeviceUpdate.class))).thenThrow(new IllegalArgumentException("invalid"));

        mockMvc.perform(put("/api/v1/devices/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceUpdate())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("DELETE /api/v1/devices/{id} -> 200 when deleted")
    void deleteDevice_ok() throws Exception {
        when(deviceService.deleteDevice(eq("1"))).thenReturn(true);

        mockMvc.perform(delete("/api/v1/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("deleted")));
    }

    @Test
    @DisplayName("DELETE /api/v1/devices/{id} -> 404 when not found")
    void deleteDevice_notFound() throws Exception {
        when(deviceService.deleteDevice(eq("2"))).thenReturn(false);

        mockMvc.perform(delete("/api/v1/devices/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("DEVICE_NOT_FOUND")));
    }

    @Test
    @DisplayName("GET /api/v1/devices/{id}/status -> 200 when present")
    void getStatus_ok() throws Exception {
        DeviceStatus status = new DeviceStatus("3", "online", LocalDateTime.now(), null);
        when(deviceService.getDeviceStatus(eq("3"))).thenReturn(Optional.of(status));

        mockMvc.perform(get("/api/v1/devices/3/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.device_id", is("3")))
                .andExpect(jsonPath("$.status", is("online")));
    }

    @Test
    @DisplayName("PATCH /api/v1/devices/{id}/status -> 200 when updated")
    void updateStatus_ok() throws Exception {
        StatusUpdateRequest req = new StatusUpdateRequest("offline", "maintenance");
        DeviceStatus updated = new DeviceStatus("3", "offline", LocalDateTime.now(), "maintenance");
        when(deviceService.updateDeviceStatus(eq("3"), any(StatusUpdateRequest.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(patch("/api/v1/devices/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("offline")))
                .andExpect(jsonPath("$.message", is("maintenance")));
    }

    @Test
    @DisplayName("PATCH /api/v1/devices/{id}/status -> 400 on bad data")
    void updateStatus_badRequest() throws Exception {
        when(deviceService.updateDeviceStatus(eq("5"), any(StatusUpdateRequest.class))).thenThrow(new IllegalArgumentException("bad"));

        mockMvc.perform(patch("/api/v1/devices/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StatusUpdateRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("GET /api/v1/devices/{id}/config -> 200 when present")
    void getConfig_ok() throws Exception {
        DeviceConfig cfg = new DeviceConfig("10", java.util.Map.of("threshold", 5), "1.0.1", true, LocalDateTime.now());
        when(deviceService.getDeviceConfig(eq("10"))).thenReturn(Optional.of(cfg));

        mockMvc.perform(get("/api/v1/devices/10/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.device_id", is("10")))
                .andExpect(jsonPath("$.settings.threshold", is(5)));
    }

    @Test
    @DisplayName("PUT /api/v1/devices/{id}/config -> 200 when updated")
    void updateConfig_ok() throws Exception {
        DeviceConfig req = new DeviceConfig("11", java.util.Map.of("mode", "eco"), "1.2.0", false, LocalDateTime.now());
        when(deviceService.updateDeviceConfig(eq("11"), any(DeviceConfig.class))).thenReturn(Optional.of(req));

        mockMvc.perform(put("/api/v1/devices/11/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settings.mode", is("eco")));
    }

    @Test
    @DisplayName("PUT /api/v1/devices/{id}/config -> 400 on bad data")
    void updateConfig_badRequest() throws Exception {
        when(deviceService.updateDeviceConfig(eq("12"), any(DeviceConfig.class))).thenThrow(new IllegalArgumentException("bad"));

        mockMvc.perform(put("/api/v1/devices/12/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceConfig())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("POST /api/v1/devices/{id}/control -> 200 when command sent")
    void controlDevice_ok() throws Exception {
        DeviceCommand cmd = new DeviceCommand("reboot", java.util.Map.of("delay", 5), "HIGH");
        when(deviceService.sendDeviceCommand(eq("20"), any(DeviceCommand.class))).thenReturn(Optional.of("cmd-1"));

        mockMvc.perform(post("/api/v1/devices/20/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Command sent successfully")))
                .andExpect(jsonPath("$.command_id", is("cmd-1")));
    }

    @Test
    @DisplayName("POST /api/v1/devices/{id}/control -> 400 on invalid command")
    void controlDevice_badCommand() throws Exception {
        when(deviceService.sendDeviceCommand(eq("21"), any(DeviceCommand.class))).thenThrow(new IllegalArgumentException("invalid"));

        mockMvc.perform(post("/api/v1/devices/21/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCommand())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_COMMAND")));
    }

    @Test
    @DisplayName("POST /api/v1/devices/{id}/control -> 404 when device not found")
    void controlDevice_notFound() throws Exception {
        when(deviceService.sendDeviceCommand(eq("22"), any(DeviceCommand.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/devices/22/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCommand())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("DEVICE_NOT_FOUND")));
    }

    @Test
    @DisplayName("POST /api/v1/devices/{id}/control -> 500 on server error")
    void controlDevice_error() throws Exception {
        when(deviceService.sendDeviceCommand(eq("23"), any(DeviceCommand.class))).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/v1/devices/23/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCommand())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")));
    }
}

