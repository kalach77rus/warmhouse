package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.CommandResponse;
import org.example.model.DeleteResponse;
import org.example.model.Device;
import org.example.model.DeviceCommand;
import org.example.model.DeviceConfig;
import org.example.model.DeviceCreate;
import org.example.model.DeviceStatus;
import org.example.model.DeviceUpdate;
import org.example.model.ErrorResponse;
import org.example.model.StatusUpdateRequest;
import org.example.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Операции управления устройствами")
public class DeviceController {
    
    private final DeviceService deviceService;

    @GetMapping("/devices")
    @Operation(summary = "Список устройств", description = "Получение всех устройств с необязательными фильтрами по типу, статусу и локации")
    @ApiResponse(responseCode = "200", description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Device.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> getAllDevices(
            @Parameter(description = "Тип устройства") @RequestParam(required = false) String type,
            @Parameter(description = "Статус устройства") @RequestParam(required = false) String status,
            @Parameter(description = "Локация устройства") @RequestParam(required = false) String location) {

        log.info("Запрос на получение списка устройств. Фильтры: type={}, status={}, location={}",
                type, status, location);

        List<Device> devices = deviceService.getAllDevices(type, status, location);
        log.info("Успешно получен список устройств. Количество: {}", devices.size());
        return ResponseEntity.ok(devices);
    }

    @PostMapping("/devices")
    @Operation(summary = "Создать устройство")
    @ApiResponse(responseCode = "201", description = "Создано",
            content = @Content(schema = @Schema(implementation = Device.class)))
    @ApiResponse(responseCode = "400", description = "Неверный запрос",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> createDevice(@RequestBody DeviceCreate deviceCreate) {
        log.info("Запрос на создание устройства: name={}, type={}, location={}",
                deviceCreate.getName(), deviceCreate.getType(), deviceCreate.getLocation());

        Device device = deviceService.createDevice(deviceCreate);
        log.info("Устройство успешно создано с ID: {}", device.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    @GetMapping("/devices/{id}")
    @Operation(summary = "Получить устройство по ID")
    @ApiResponse(responseCode = "200", description = "Найдено",
            content = @Content(schema = @Schema(implementation = Device.class)))
    @ApiResponse(responseCode = "404", description = "Не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> getDeviceById(@PathVariable String id) {
        log.info("Запрос на получение устройства по ID: {}", id);

        Optional<Device> device = deviceService.getDeviceById(id);
        Device found = device.orElseThrow(() -> {
            log.warn("Устройство не найдено: ID={}", id);
            return new NoSuchElementException("Device not found");
        });
        log.info("Устройство найдено: ID={}, name={}", id, found.getName());
        return ResponseEntity.ok(found);
    }

    @PutMapping("/devices/{id}")
    @Operation(summary = "Обновить устройство")
    @ApiResponse(responseCode = "200", description = "Обновлено",
            content = @Content(schema = @Schema(implementation = Device.class)))
    @ApiResponse(responseCode = "400", description = "Неверный запрос",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> updateDevice(@PathVariable String id, @RequestBody DeviceUpdate deviceUpdate) {
        log.info("Запрос на обновление устройства: ID={}", id);

        Optional<Device> updatedDevice = deviceService.updateDevice(id, deviceUpdate);
        Device result = updatedDevice.orElseThrow(() -> {
            log.warn("Устройство не найдено для обновления: ID={}", id);
            return new NoSuchElementException("Device not found");
        });
        log.info("Устройство успешно обновлено: ID={}", id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/devices/{id}")
    @Operation(summary = "Удалить устройство")
    @ApiResponse(responseCode = "200", description = "Удалено",
            content = @Content(schema = @Schema(implementation = DeleteResponse.class)))
    @ApiResponse(responseCode = "404", description = "Не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> deleteDevice(@PathVariable String id) {
        log.info("Запрос на удаление устройства: ID={}", id);

        boolean deleted = deviceService.deleteDevice(id);
        if (!deleted) {
            log.warn("Устройство не найдено для удаления: ID={}", id);
            throw new NoSuchElementException("Device not found");
        }
        log.info("Устройство успешно удалено: ID={}", id);
        return ResponseEntity.ok(new DeleteResponse("Device deleted successfully"));
    }

    @GetMapping("/devices/{id}/status")
    @Operation(summary = "Получить статус устройства")
    @ApiResponse(responseCode = "200", description = "ОК",
            content = @Content(schema = @Schema(implementation = DeviceStatus.class)))
    @ApiResponse(responseCode = "404", description = "Не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> getDeviceStatus(@PathVariable String id) {
        log.info("Запрос на получение статуса устройства: ID={}", id);

        Optional<DeviceStatus> status = deviceService.getDeviceStatus(id);
        DeviceStatus result = status.orElseThrow(() -> {
            log.warn("Устройство не найдено для получения статуса: ID={}", id);
            return new NoSuchElementException("Device not found");
        });
        log.info("Статус устройства получен: ID={}, status={}", id, result.getStatus());
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/devices/{id}/status")
    @Operation(summary = "Обновить статус устройства")
    @ApiResponse(responseCode = "200", description = "ОК",
            content = @Content(schema = @Schema(implementation = DeviceStatus.class)))
    @ApiResponse(responseCode = "400", description = "Неверный запрос",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> updateDeviceStatus(@PathVariable String id, @RequestBody StatusUpdateRequest request) {
        log.info("Запрос на обновление статуса устройства: ID={}, новый статус={}", id, request.getStatus());

        Optional<DeviceStatus> updatedStatus = deviceService.updateDeviceStatus(id, request);
        DeviceStatus result = updatedStatus.orElseThrow(() -> {
            log.warn("Устройство не найдено для обновления статуса: ID={}", id);
            return new NoSuchElementException("Device not found");
        });
        log.info("Статус устройства успешно обновлен: ID={}, status={}", id, result.getStatus());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/devices/{id}/config")
    @Operation(summary = "Получить конфигурацию устройства")
    @ApiResponse(responseCode = "200", description = "ОК",
            content = @Content(schema = @Schema(implementation = DeviceConfig.class)))
    @ApiResponse(responseCode = "404", description = "Не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> getDeviceConfig(@PathVariable String id) {
        log.info("Запрос на получение конфигурации устройства: ID={}", id);

        Optional<DeviceConfig> config = deviceService.getDeviceConfig(id);
        DeviceConfig result = config.orElseThrow(() -> {
            log.warn("Устройство не найдено для получения конфигурации: ID={}", id);
            return new NoSuchElementException("Device not found");
        });
        log.info("Конфигурация устройства получена: ID={}", id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/devices/{id}/config")
    @Operation(summary = "Обновить конфигурацию устройства")
    @ApiResponse(responseCode = "200", description = "ОК",
            content = @Content(schema = @Schema(implementation = DeviceConfig.class)))
    @ApiResponse(responseCode = "400", description = "Неверный запрос",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> updateDeviceConfig(@PathVariable String id, @RequestBody DeviceConfig deviceConfig) {
        log.info("Запрос на обновление конфигурации устройства: ID={}", id);

        Optional<DeviceConfig> updatedConfig = deviceService.updateDeviceConfig(id, deviceConfig);
        DeviceConfig result = updatedConfig.orElseThrow(() -> {
            log.warn("Устройство не найдено для обновления конфигурации: ID={}", id);
            return new NoSuchElementException("Device not found");
        });
        log.info("Конфигурация устройства успешно обновлена: ID={}", id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/devices/{id}/control")
    @Operation(summary = "Отправить команду устройству")
    @ApiResponse(responseCode = "200", description = "ОК",
            content = @Content(schema = @Schema(implementation = CommandResponse.class)))
    @ApiResponse(responseCode = "400", description = "Неверная команда",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> controlDevice(@PathVariable String id, @RequestBody DeviceCommand command) {
        log.info("Запрос на управление устройством: ID={}, команда={}, приоритет={}",
                id, command.getCommand(), command.getPriority());

        try {
            Optional<String> commandId = deviceService.sendDeviceCommand(id, command);
            String sentCommandId = commandId.orElseThrow(() -> {
                log.warn("Устройство не найдено для отправки команды: ID={}", id);
                return new NoSuchElementException("Device not found");
            });
            log.info("Команда успешно отправлена устройству: ID={}, commandId={}", id, sentCommandId);
            return ResponseEntity.ok(new CommandResponse("Command sent successfully", sentCommandId));
        } catch (IllegalArgumentException e) {
            log.warn("Неверная команда для устройства {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid command", "INVALID_COMMAND"));
        }
    }
}
