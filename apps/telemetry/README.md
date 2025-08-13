## Telemetry Service

Сервис телеметрии собирает измерения температуры из внешнего сервиса (`temperature-api`) и сохраняет их в БД (H2). Предоставляет REST-эндпоинты для получения телеметрии и документацию Swagger.

### Архитектура и потоки данных
- **Планировщик**: каждые `telemetry.poll-interval-ms` мс вызывает внешний `temperature-api` по `GET {base-url}/temperature/{sensorId}` и сохраняет запись в H2.
- **Хранение**: таблица `temperature` (JPA/Hibernate, `ddl-auto=update`).
- **API**: чтение количества записей, последней записи по `location`/`sensorId`, а также список всех записей.

### Требования
- Java 17
- Maven 3.9+

### Быстрый старт (локально)
1) Перейдите в каталог сервиса:
```bash
cd apps/telemetry
```
2) Запустите `temperature-api` (по умолчанию он должен работать на `http://localhost:8081`). Если он уже запущен — пропустите. Иначе в другом терминале:
```bash
cd ../temperature-api && mvn spring-boot:run
```
3) Запустите `telemetry`:
```bash
mvn spring-boot:run
```
По умолчанию сервис слушает порт `8091`.

### Конфигурация
Основные настройки определены в `src/main/resources/application.yml`:
```yaml
server:
  port: 8091

spring:
  datasource:
    url: jdbc:h2:mem:telemetrydb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: admin
    password: admin
  jpa:
    hibernate:
      ddl-auto: update
  h2:
    console:
      enabled: true
      path: /h2

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui

temperature:
  api:
    base-url: http://localhost:8081

telemetry:
  poll-interval-ms: 5000
```

Можно переопределять свойства переменными окружения (пример для PowerShell/Windows):
```powershell
$Env:TEMPERATURE_API_BASE_URL = "http://localhost:8081"
$Env:TELEMETRY_POLL_INTERVAL_MS = "5000"
$Env:SERVER_PORT = "8091"
mvn spring-boot:run
```

### База данных
- Тип: H2 in-memory
- Консоль: `http://localhost:8091/h2` (JDBC URL: `jdbc:h2:mem:telemetrydb`, user: `admin`, password: `admin`).
- Схема записи `temperature` (ключевые поля): `id`, `sensor_id`, `temperature_value` (`value`), `unit`, `timestamp`, `location`, `status`, `sensor_type`, `description`.

Пример запроса в H2-консоли:
```sql
select id, sensor_id, temperature_value, unit, timestamp, location from temperature order by timestamp desc;
```

### REST API
Базовый префикс: `http://localhost:8091/api/v1`

- Получить количество записей:
```bash
curl http://localhost:8091/api/v1/count
```

- Получить последнюю запись по локации:
```bash
curl "http://localhost:8091/api/v1/temperature?location=Kitchen"
```
Пример ответа:
```json
{
  "value": 22.14,
  "unit": "Celsius",
  "timestamp": "2024-07-10T12:30:00Z",
  "location": "Kitchen",
  "status": "active",
  "sensor_id": "3",
  "sensor_type": "temperature",
  "description": "Temperature sensor 3"
}
```

- Получить последнюю запись по `sensorId`:
```bash
curl http://localhost:8091/api/v1/temperature/1
```

- Получить все записи (отсортированы по времени убыванию):
```bash
curl http://localhost:8091/api/v1/temperatures
```
Элементы массива соответствуют `TemperatureDto`:
```json
{
  "id": 1,
  "sensorId": "1",
  "value": 22.14,
  "unit": "C",
  "timestamp": "2024-07-10T12:30:00Z",
  "location": "Kitchen",
  "status": "OK",
  "sensorType": "thermometer",
  "description": "Temperature sensor 1"
}
```

### Swagger и Actuator
- Swagger UI: `http://localhost:8091/swagger-ui`
- OpenAPI JSON: `http://localhost:8091/v3/api-docs`
- Actuator: `http://localhost:8091/actuator/health`, `http://localhost:8091/actuator/info`

### Планировщик опроса
- Компонент: `org.example.configuration.TemperaturePoller`
- Период: `telemetry.poll-interval-ms` (по умолчанию 5000 мс)
- Источник данных: `temperature-api` (`org.example.client.TemperatureClient`), URL: `{temperature.api.base-url}/temperature/{sensorId}`

### Сборка и тесты
```bash
mvn -q -DskipTests=false clean verify
```

### Обработка ошибок
Ответ ошибки имеет форму:
```json
{ "error": "Описание", "code": "КОД" }
```
Типовые коды: `BAD_REQUEST`, `DEVICE_NOT_FOUND`, `TYPE_MISMATCH`, `MISSING_PARAMETER`, `MALFORMED_JSON`, `VALIDATION_ERROR`, `METHOD_NOT_ALLOWED`, `INTERNAL_ERROR`.

### Полезные замечания
- Для корректной работы опроса должен быть доступен `temperature-api` по `temperature.api.base-url`.
- В тестовом стенде `sensorId` генерируется из диапазона `1..3`, а `location` соответствует: `1 → Living Room`, `2 → Bedroom`, `3 → Kitchen`.


