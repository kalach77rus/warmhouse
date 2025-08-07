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

        try {
            List<Device> devices = deviceService.getAllDevices(type, status, location);
            log.info("Успешно получен список устройств. Количество: {}", devices.size());
            return ResponseEntity.ok(devices);
        } catch (Exception e) {
            log.error("Ошибка при получении списка устройств", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
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

        try {
            Device device = deviceService.createDevice(deviceCreate);
            log.info("Устройство успешно создано с ID: {}", device.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(device);
        } catch (IllegalArgumentException e) {
            log.warn("Неверные данные при создании устройства: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid request data", "BAD_REQUEST"));
        } catch (Exception e) {
            log.error("Ошибка при создании устройства", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
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
        if (device.isPresent()) {
            log.info("Устройство найдено: ID={}, name={}", id, device.get().getName());
            return ResponseEntity.ok(device.get());
        } else {
            log.warn("Устройство не найдено: ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Device not found", "DEVICE_NOT_FOUND"));
        }
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

        try {
            Optional<Device> updatedDevice = deviceService.updateDevice(id, deviceUpdate);
            if (updatedDevice.isPresent()) {
                log.info("Устройство успешно обновлено: ID={}", id);
                return ResponseEntity.ok(updatedDevice.get());
            } else {
                log.warn("Устройство не найдено для обновления: ID={}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Device not found", "DEVICE_NOT_FOUND"));
            }
        } catch (IllegalArgumentException e) {
            log.warn("Неверные данные при обновлении устройства {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid request data", "BAD_REQUEST"));
        } catch (Exception e) {
            log.error("Ошибка при обновлении устройства: ID={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
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

        try {
            boolean deleted = deviceService.deleteDevice(id);
            if (deleted) {
                log.info("Устройство успешно удалено: ID={}", id);
                return ResponseEntity.ok(new DeleteResponse("Device deleted successfully"));
            } else {
                log.warn("Устройство не найдено для удаления: ID={}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Device not found", "DEVICE_NOT_FOUND"));
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении устройства: ID={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
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
        if (status.isPresent()) {
            log.info("Статус устройства получен: ID={}, status={}", id, status.get().getStatus());
            return ResponseEntity.ok(status.get());
        } else {
            log.warn("Устройство не найдено для получения статуса: ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Device not found", "DEVICE_NOT_FOUND"));
        }
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

        try {
            Optional<DeviceStatus> updatedStatus = deviceService.updateDeviceStatus(id, request);
            if (updatedStatus.isPresent()) {
                log.info("Статус устройства успешно обновлен: ID={}, status={}", id, updatedStatus.get().getStatus());
                return ResponseEntity.ok(updatedStatus.get());
            } else {
                log.warn("Устройство не найдено для обновления статуса: ID={}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Device not found", "DEVICE_NOT_FOUND"));
            }
        } catch (IllegalArgumentException e) {
            log.warn("Неверные данные при обновлении статуса устройства {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid request data", "BAD_REQUEST"));
        }
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
        if (config.isPresent()) {
            log.info("Конфигурация устройства получена: ID={}", id);
            return ResponseEntity.ok(config.get());
        } else {
            log.warn("Устройство не найдено для получения конфигурации: ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Device not found", "DEVICE_NOT_FOUND"));
        }
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

        try {
            Optional<DeviceConfig> updatedConfig = deviceService.updateDeviceConfig(id, deviceConfig);
            if (updatedConfig.isPresent()) {
                log.info("Конфигурация устройства успешно обновлена: ID={}", id);
                return ResponseEntity.ok(updatedConfig.get());
            } else {
                log.warn("Устройство не найдено для обновления конфигурации: ID={}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Device not found", "DEVICE_NOT_FOUND"));
            }
        } catch (IllegalArgumentException e) {
            log.warn("Неверные данные при обновлении конфигурации устройства {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid request data", "BAD_REQUEST"));
        }
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
            if (commandId.isPresent()) {
                log.info("Команда успешно отправлена устройству: ID={}, commandId={}", id, commandId.get());
                return ResponseEntity.ok(new CommandResponse("Command sent successfully", commandId.get()));
            } else {
                log.warn("Устройство не найдено для отправки команды: ID={}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Device not found", "DEVICE_NOT_FOUND"));
            }
        } catch (IllegalArgumentException e) {
            log.warn("Неверная команда для устройства {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid command", "INVALID_COMMAND"));
        } catch (Exception e) {
            log.error("Ошибка при отправке команды устройству: ID={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
    }
}
