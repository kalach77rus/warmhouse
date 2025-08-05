# Temperature API

Spring Boot приложение для имитации датчика температуры с поддержкой Docker-среды.

## Технологии

- **Java**: 17
- **Spring Boot**: 3.3.0
- **Maven**: для сборки проекта
- **Docker**: для контейнеризации

## Структура проекта

```
temperature-api/
├── src/
│   └── main/
│       ├── java/
│       │   └── org/example/
│       │       ├── TemperatureApiApplication.java
│       │       └── controller/
│       │           └── TemperatureController.java
│       └── resources/
│           └── application.properties
├── pom.xml
├── Dockerfile
└── README.md
```

## API Endpoints

### 1. Получение температуры по локации
- **Метод**: `GET`
- **Путь**: `/temperature`
- **Параметры**: `location` (обязательный)
- **Пример**: `GET /temperature?location=Living Room`

### 2. Получение температуры по ID датчика
- **Метод**: `GET`
- **Путь**: `/temperature/{sensorId}`
- **Параметры**: `sensorId` (в пути)
- **Пример**: `GET /temperature/1`

## Примеры ответов

### Ответ для локации
```json
{
  "value": 18.75,
  "unit": "Celsius",
  "timestamp": "2024-01-15T10:30:45.123Z",
  "location": "Living Room",
  "status": "active",
  "sensor_id": "1",
  "sensor_type": "temperature",
  "description": "Temperature sensor 1"
}
```

### Ответ для ID датчика
```json
{
  "value": 22.34,
  "unit": "Celsius",
  "timestamp": "2024-01-15T10:30:45.123Z",
  "location": "Living Room",
  "status": "active",
  "sensor_id": "1",
  "sensor_type": "temperature",
  "description": "Temperature sensor 1"
}
```

## Запуск

### Локальная разработка

```bash
# Компиляция проекта
mvn clean compile

# Запуск тестов
mvn test

# Локальный запуск (порт 8081)
mvn spring-boot:run
```

### Docker

```bash
# Сборка образа
docker build -t temperature-api .

# Запуск контейнера
docker run -p 8081:8081 temperature-api
```

## Конфигурация

### Основные настройки (application.properties)
- **Порт**: 8081
- **Контекст**: /
- **Логирование**: INFO уровень
- **Actuator**: health и info endpoints

### Health Check
- **URL**: `http://localhost:8081/actuator/health`
- **Интервал**: 30 секунд
- **Таймаут**: 3 секунды

## Тестирование API

```bash
# Проверка health check
curl http://localhost:8081/actuator/health

# Получение температуры по локации
curl "http://localhost:8081/temperature?location=Living%20Room"
curl "http://localhost:8081/temperature?location=Bedroom"
curl "http://localhost:8081/temperature?location=Kitchen"

# Получение температуры по ID датчика
curl "http://localhost:8081/temperature/1"
curl "http://localhost:8081/temperature/2"
curl "http://localhost:8081/temperature/3"
```

## Маппинг локаций и датчиков

| ID датчика | Локация |
|------------|---------|
| 1 | Living Room |
| 2 | Bedroom |
| 3 | Kitchen |
| 0 | Unknown (по умолчанию) |

## Мониторинг

- **Health Check**: `GET /actuator/health`
- **Info**: `GET /actuator/info`
- **Логирование**: консольный вывод с timestamp

## Безопасность

- Использование непривилегированного пользователя в Docker
- Изолированная Docker сеть
- Ограничение доступа к портам
- Многоэтапная сборка Docker образа

## Разработка

### Требования
- Java 17+
- Maven 3.6+
- Docker (опционально)

### Команды для разработки
```bash
# Очистка и компиляция
mvn clean compile

# Запуск тестов
mvn test

# Сборка JAR
mvn clean package

# Запуск с профилем
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Docker команды
```bash
# Сборка образа
docker build -t temperature-api .

# Запуск контейнера
docker run -d -p 8081:8081 --name temperature-api temperature-api

# Просмотр логов
docker logs temperature-api

# Остановка контейнера
docker stop temperature-api
docker rm temperature-api
```