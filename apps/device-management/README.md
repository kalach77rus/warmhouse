### Device Management Service

Коротко: REST‑сервис управления устройствами умного дома. Поддерживает CRUD устройств, чтение/изменение статуса, конфигурации и отправку команд.

### Возможности
- **CRUD устройств**: создание, чтение, обновление, удаление (`/api/v1/devices`)
- **Статус устройства**: получить/обновить (`/api/v1/devices/{id}/status`)
- **Конфигурация**: получить/обновить (`/api/v1/devices/{id}/config`)
- **Команды управлению**: отправка команд устройству (`/api/v1/devices/{id}/control`)
- **Health‑check**: `/health`

### Технологии
- Java 17, Spring Boot 3.3.x (Web, Actuator)
- springdoc‑openapi 2.5.x (Swagger UI)
- Lombok
- Maven

### Структура проекта
```
apps/device-management/
  ├─ pom.xml
  ├─ src/
  │  ├─ main/java/org/example/
  │  │  ├─ DeviceManagementApplication.java
  │  │  ├─ controller/
  │  │  │  ├─ DeviceController.java
  │  │  │  ├─ HealthController.java
  │  │  │  └─ GlobalExceptionHandler.java
  │  │  ├─ model/ (Device, DeviceCreate, DeviceUpdate, DeviceStatus, DeviceConfig, ...)
  │  │  └─ service/ (DeviceService — заглушки бизнес‑логики)
  │  └─ main/resources/application.properties
  │
  │  └─ test/java/org/example/controller/ (интеграционные MVC‑тесты контроллеров)
  
schemas/device-service-api.yaml — спецификация OpenAPI (статичная)
diagrams/components/device-service-components.puml — компоненты
diagrams/code/device-service-code.puml — обзор кода/слоёв
```

### Быстрый старт
Требования: JDK 17+, Maven 3.9+

Запуск из исходников:
```bash
mvn -q -f apps/device-management/pom.xml spring-boot:run
```

Сборка и запуск JAR:
```bash
mvn -q -f apps/device-management/pom.xml clean package
java -jar apps/device-management/target/device-management-1.0.0.jar
```

По умолчанию сервис слушает порт `8082`.

### Конфигурация
`apps/device-management/src/main/resources/application.properties`
```properties
server.port=8082
spring.application.name=device-management
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
```
Параметры можно переопределять через переменные окружения по правилам Spring Boot (например, `SERVER_PORT=9090`).

### API
Базовый префикс: `/api/v1` (кроме `/health`).

- `GET /health` — проверка состояния сервиса
- `GET /api/v1/devices` — список устройств (фильтры: `type`, `status`, `location`)
- `POST /api/v1/devices` — создать устройство
- `GET /api/v1/devices/{id}` — получить устройство по ID
- `PUT /api/v1/devices/{id}` — обновить устройство
- `DELETE /api/v1/devices/{id}` — удалить устройство
- `GET /api/v1/devices/{id}/status` — статус устройства
- `PATCH /api/v1/devices/{id}/status` — обновить статус
- `GET /api/v1/devices/{id}/config` — конфигурация устройства
- `PUT /api/v1/devices/{id}/config` — обновить конфигурацию
- `POST /api/v1/devices/{id}/control` — отправить команду

Модели (поля по коду):
- `Device` — id, name, type, location, status, manufacturer, model, firmware_version, ip_address, mac_address, created_at, updated_at
- `DeviceCreate`/`DeviceUpdate` — name, type, location, manufacturer, model, ip_address, mac_address
- `DeviceStatus` — device_id, status, last_seen, message
- `StatusUpdateRequest` — status, message
- `DeviceConfig` — device_id, settings (Map), firmware_version, update_available, last_updated
- `DeviceCommand` — command, parameters (Map), priority

Полная спецификация: `schemas/device-service-api.yaml`.

### Примеры
Получить список устройств:
```bash
curl -s "http://localhost:8082/api/v1/devices?type=sensor"
```

Создать устройство:
```bash
curl -s -X POST "http://localhost:8082/api/v1/devices" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Thermostat",
    "type":"sensor",
    "location":"kitchen",
    "manufacturer":"Acme",
    "model":"T1000",
    "ip_address":"10.0.0.2",
    "mac_address":"AA:BB:CC"
  }'
```

Отправить команду устройству:
```bash
curl -s -X POST "http://localhost:8082/api/v1/devices/42/control" \
  -H "Content-Type: application/json" \
  -d '{ "command":"reboot", "parameters": {"delay": 5}, "priority":"HIGH" }'
```

### Обработка ошибок
Единый формат ошибок:
```json
{ "error": "Human readable message", "code": "MACHINE_CODE" }
```
Коды по коду контроллеров/хендлеров: `BAD_REQUEST`, `DEVICE_NOT_FOUND`, `TYPE_MISMATCH`, `MISSING_PARAMETER`, `MALFORMED_JSON`, `VALIDATION_ERROR`, `METHOD_NOT_ALLOWED`, `INTERNAL_ERROR`, `INVALID_COMMAND`.

### Swagger / OpenAPI
- Swagger UI (runtime): `http://localhost:8082/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`
- Статичный OpenAPI: `schemas/device-service-api.yaml`

Аннотации OpenAPI заданы в `DeviceManagementApplication.java` и контроллерах.

### Тесты
Запуск тестов:
```bash
mvn -q -f apps/device-management/pom.xml test
```
Покрыты основные сценарии контроллера устройств и health‑check (см. `src/test/java/org/example/controller`).

### Диаграммы
- Компоненты: `diagrams/components/device-service-components.puml`
- Обзор кода/слоёв: `diagrams/code/device-service-code.puml`

В репозитории есть `plantuml.jar` — можно отрендерить локально, например:
```bash
java -jar plantuml.jar diagrams/components/device-service-components.puml
```

### Замечания по реализации
- Класс `DeviceService` содержит методы‑заглушки; интеграция с БД/брокером событий пока не реализована.
- Логгирование включено, формат на консоль настроен в `application.properties`.

### Лицензия
В метаданных OpenAPI указана лицензия MIT.

