@startuml
 
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Component.puml

title C4 — Уровень компонентов (Components)

System_Boundary(device_mgmt_boundary, "Микросервис Device Manager") {
    Component(device_api, "Device API", "REST/gRPC", "Внешний API для управления устройствами")
    Component(command_processor, "Command Processor", "Go", "Обработка и валидация команд")
    Component(device_state_manager, "Device State Manager", "Go", "Управление состояниями устройств")
    Component(device_registry, "Device Registry", "Go", "Регистрация устройств")
    Component(driver_manager, "Driver Manager", "Go", "Управление драйверами устройств")
    Component(protocol_adapter, "Protocol Adapter", "Go", "Адаптация протоколов")
}

System_Boundary(scenario_boundary, "Микросервис Scenario Engine") {
    Component(scenario_api, "Scenario API", "REST", "API для управления сценариями")
    Component(rule_engine, "Rule Engine", "Python", "Выполнение правил и условий")
    Component(trigger_manager, "Trigger Manager", "Python", "Управление триггерами")
    Component(action_executor, "Action Executor", "Python", "Выполнение действий сценария")
    Component(scenario_scheduler, "Scenario Scheduler", "Python", "Планирование сценариев")
}

System_Boundary(message_boundary, "Message Broker") {
    Component(mqtt_broker, "MQTT Broker", "Mosquitto", "MQTT сообщения")
    Component(amqp_broker, "AMQP Broker", "RabbitMQ", "Внутренние события")
}

' === ВНУТРЕННИЕ СВЯЗИ УПРАВЛЕНИЯ УСТРОЙСТВАМИ ===
Rel(device_api, command_processor, "Передает команды", "gRPC")
Rel(command_processor, device_state_manager, "Обновляет состояние", "gRPC")
Rel(command_processor, protocol_adapter, "Отправляет команду", "gRPC")
Rel(device_registry, command_processor, "Проверяет устройство", "gRPC")
Rel(device_registry, driver_manager, "Загружает драйвер", "gRPC")
Rel(driver_manager, protocol_adapter, "Предоставляет драйвер", "gRPC")
Rel(protocol_adapter, mqtt_broker, "Публикует команды", "MQTT")
Rel(mqtt_broker, protocol_adapter, "Получает телеметрию", "MQTT")
Rel(protocol_adapter, device_state_manager, "Обновляет телеметрию", "gRPC")

' === ВНУТРЕННИЕ СВЯЗИ СЦЕНАРИЕВ ===
Rel(scenario_api, rule_engine, "Загружает сценарии", "JSON/RPC")
Rel(trigger_manager, rule_engine, "Активирует триггеры", "Event bus")
Rel(rule_engine, action_executor, "Выполняет действия", "gRPC")
Rel(scenario_scheduler, trigger_manager, "Запускает по расписанию", "Event bus")
Rel(action_executor, device_api, "Вызывает команды", "gRPC")

' === СВЯЗИ С БРОКЕРОМ СООБЩЕНИЙ ===
Rel(device_state_manager, amqp_broker, "Публикует события состояния", "AMQP")
Rel(trigger_manager, amqp_broker, "Подписывается на события", "AMQP")
Rel(amqp_broker, trigger_manager, "Доставляет события", "AMQP")

@enduml
