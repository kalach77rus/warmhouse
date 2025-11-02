@startuml

title Context diagram С4

top to bottom direction

!includeurl https://raw.githubusercontent.com/RicardoNiepel/C4-PlantUML/master/C4_Component.puml

Person(user, "Пользователь", "Управление отоплением, проверка температуры")
Person(admin, "Специалист", "Подключение и регистрирация датчика")
System(System, "Умный дом", "Управляет датчиками и регистрирует их при включении")

Container(api, "Third-Party API", "Внешнее API датчиков отопления", "Uses REST API, JSON data format", $tags="backendContainer")
ContainerDb(DB, "База данных", "База с данными пользователей и датчиков")

Rel(user, System, "Управляет отоплением и проверяет температуру")
Rel(admin, System, "Подключает и регистрирует датчик")
Rel(System, api, "Обмен данными с датчиками")
Rel(System, DB, "Хранение данных")

@enduml