@startuml
!NEW_C4_STYLE=1
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Container.puml

title C4 — Уровень контейнеров (Containers)

Person(user, "Пользователь", "Самостоятельно настраивает устройства и сценарии")

System_Boundary(saas_system, "SaaS платформа «Тёплый дом»") {
    Container(web_app, "Веб приложение", "React/Angular", "Настройка устройств, создание сценариев, просмотр телеметрии")
    Container(mobile_app, "Мобильное приложение", "iOS/Android", "Управление устройствами, уведомления, мониторинг")
    
    Container(backend_api, "API Gateway", "Go", "Единая точка входа, аутентификация, маршрутизация")
    ContainerDb(database, "База данных", "PostgreSQL", "Пользователи, устройства, сценарии, телеметрия, настройки")
    Container(message_broker, "Message Broker", "RabbitMQ/MQTT", "Асинхронная коммуникация с устройствами")
    
    Container(device_manager, "Device Manager", "Go", "Регистрация устройств, управление подключениями, драйверы устройств")
    Container(scenario_engine, "Scenario Engine", "Python", "Выполнение сценариев, автоматизация, триггеры")
}

System_Ext(partner_devices, "Устройства партнеров", "Поддерживаемые протоколы: MQTT, Zigbee, Z-Wave")

Boundary(devices_boundary, "Умные устройства") {
    Container(heating, "Система отопления", "Термостаты, котлы", "Поддержка OpenTherm/MQTT")
    Container(lighting, "Освещение", "Умные лампы, выключатели", "Zigbee/Z-Wave")
    Container(gates, "Автоматические ворота", "Контроллеры ворот", "MQTT/HTTP")
    Container(security, "Система наблюдения", "Камеры, датчики", "ONVIF/MQTT")
}

' === ПОЛЬЗОВАТЕЛЬСКИЕ ИНТЕРФЕЙСЫ ===
Rel(user, web_app, "Настраивает устройства", "HTTPS")
Rel(user, mobile_app, "Управляет в реальном времени", "HTTPS/Push")

' === ФРОНТЕНД -> БЭКЕНД ===
Rel(web_app, backend_api, "API вызовы", "REST/HTTPS")
Rel(mobile_app, backend_api, "API вызовы", "REST/HTTPS")

' === ЯДРО СИСТЕМЫ ===
Rel(backend_api, database, "CRUD операции", "SQL")
Rel(backend_api, message_broker, "Отправка команд", "AMQP")
Rel(backend_api, device_manager, "Управление устройствами", "gRPC")
Rel(backend_api, scenario_engine, "Запуск сценариев", "HTTP")

' === УПРАВЛЕНИЕ УСТРОЙСТВАМИ ===
Rel(device_manager, message_broker, "Команды устройствам", "AMQP")
Rel(device_manager, database, "Состояния устройств", "SQL")
Rel(device_manager, partner_devices, "Интеграция с партнерами", "REST/Webhooks")

' === ДВИЖОК СЦЕНАРИЕВ ===
Rel(scenario_engine, message_broker, "Подписка на события", "AMQP")
Rel(scenario_engine, database, "Чтение сценариев", "SQL")

' === КОММУНИКАЦИЯ С УСТРОЙСТВАМИ ===
BiRel(message_broker, heating, "Управление температурой", "MQTT")
BiRel(message_broker, lighting, "Включение/выключение", "MQTT/Zigbee")
BiRel(message_broker, gates, "Открытие/закрытие", "MQTT")
BiRel(message_broker, security, "Управление и мониторинг", "MQTT/ONVIF")

@enduml