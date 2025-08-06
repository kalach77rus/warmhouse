# API Documentation

Документация API для микросервисной архитектуры "Умный дом"

## Описание

В данной папке содержатся OpenAPI спецификации для двух основных сервисов системы:

### 1. Telemetry Service API (`telemetry-service-api.yaml`)

Сервис для сбора и обработки телеметрии умного дома.

**Основные возможности:**
- Управление датчиками (CRUD операции)
- Сбор и обработка телеметрических данных
- Агрегация данных по временным интервалам
- Валидация телеметрических данных
- Интеграция с внешним Temperature API
- Обработка событий из message broker

**Эндпоинты:**
- `GET /health` - проверка состояния сервиса
- `GET /api/v1/sensors` - получение списка датчиков
- `POST /api/v1/sensors` - создание нового датчика
- `GET /api/v1/sensors/{id}` - получение датчика по ID
- `PUT /api/v1/sensors/{id}` - обновление датчика
- `DELETE /api/v1/sensors/{id}` - удаление датчика
- `PATCH /api/v1/sensors/{id}/value` - обновление значения датчика
- `GET /api/v1/sensors/temperature/{location}` - получение температуры по местоположению
- `GET /api/v1/telemetry` - получение телеметрических данных
- `POST /api/v1/telemetry` - отправка телеметрических данных
- `GET /api/v1/telemetry/aggregated` - получение агрегированных данных
- `GET /api/v1/telemetry/events` - получение событий телеметрии

### 2. Device Management Service API (`device-service-api.yaml`)

Сервис для управления устройствами умного дома и их конфигурацией.

**Основные возможности:**
- Управление устройствами (CRUD операции)
- Мониторинг статуса устройств
- Конфигурация устройств
- Отправка команд управления

**Эндпоинты:**
- `GET /health` - проверка состояния сервиса
- `GET /api/v1/devices` - получение списка устройств
- `POST /api/v1/devices` - создание нового устройства
- `GET /api/v1/devices/{id}` - получение устройства по ID
- `PUT /api/v1/devices/{id}` - обновление устройства
- `DELETE /api/v1/devices/{id}` - удаление устройства
- `GET /api/v1/devices/{id}/status` - получение статуса устройства
- `PATCH /api/v1/devices/{id}/status` - обновление статуса устройства
- `GET /api/v1/devices/{id}/config` - получение конфигурации устройства
- `PUT /api/v1/devices/{id}/config` - обновление конфигурации устройства
- `POST /api/v1/devices/{id}/control` - отправка команды управления

## Использование

### Просмотр документации

1. **Swagger UI**: Откройте файлы `.yaml` в Swagger UI для интерактивного просмотра
2. **Redoc**: Используйте Redoc для генерации красивой документации
3. **Postman**: Импортируйте спецификации в Postman для тестирования

### Генерация клиентов

```bash
# Генерация клиента для Telemetry Service
openapi-generator generate -i telemetry-service-api.yaml -g java -o telemetry-client

# Генерация клиента для Device Service
openapi-generator generate -i device-service-api.yaml -g java -o device-client
```

### Валидация спецификаций

```bash
# Проверка синтаксиса
swagger-cli validate telemetry-service-api.yaml
swagger-cli validate device-service-api.yaml
```

## Архитектурные особенности

### Telemetry Service
- **Технология**: Java + Spring Boot
- **База данных**: MongoDB
- **Интеграции**: Внешний Temperature API, Message Broker (RabbitMQ)
- **Порт**: 8082

### Device Management Service
- **Технология**: Java + Spring Boot
- **База данных**: PostgreSQL
- **Интеграции**: Message Broker (RabbitMQ)
- **Порт**: 8082

## Модели данных

### Telemetry Service

**Sensor** - модель датчика:
- `id` (integer) - уникальный идентификатор
- `name` (string) - название датчика
- `type` (string) - тип датчика (temperature)
- `location` (string) - местоположение
- `value` (number) - текущее значение
- `unit` (string) - единица измерения
- `status` (string) - статус датчика
- `last_updated` (datetime) - время последнего обновления
- `created_at` (datetime) - время создания

**TelemetryData** - модель телеметрических данных:
- `id` (string) - уникальный идентификатор
- `sensor_id` (string) - ID датчика
- `sensor_type` (string) - тип датчика
- `value` (number) - значение
- `unit` (string) - единица измерения
- `location` (string) - местоположение
- `timestamp` (datetime) - время измерения
- `metadata` (object) - дополнительные данные

### Device Management Service

**Device** - модель устройства:
- `id` (string) - уникальный идентификатор
- `name` (string) - название устройства
- `type` (string) - тип устройства (light, thermostat, camera, lock, sensor, switch)
- `location` (string) - местоположение
- `status` (string) - статус (online, offline, error, maintenance)
- `manufacturer` (string) - производитель
- `model` (string) - модель
- `firmware_version` (string) - версия прошивки
- `ip_address` (string) - IP адрес
- `mac_address` (string) - MAC адрес
- `created_at` (datetime) - время создания
- `updated_at` (datetime) - время обновления

**DeviceCommand** - модель команды управления:
- `command` (string) - команда для устройства
- `parameters` (object) - параметры команды
- `priority` (string) - приоритет (low, normal, high, urgent)

## Стандарты

Документация соответствует стандарту OpenAPI 3.0.3 и включает:

- Полное описание всех эндпоинтов
- Схемы данных (модели)
- Примеры запросов и ответов
- Коды ошибок
- Теги для группировки операций
- Контактную информацию

## Разработка

При внесении изменений в API:

1. Обновите соответствующую спецификацию
2. Обновите версию API
3. Добавьте новые примеры
4. Обновите документацию в README

## Контакты

- **Команда**: Smart Home Team
- **Email**: support@smarthome.com 