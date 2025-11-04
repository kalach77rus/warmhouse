@startuml
!define table(x) entity x << (T,#FFAAAA) >>
hide methods

title ER-диаграмма

table(users) {
  + id: UUID <<PK>>
  --
  * email: string
  * password_hash: string
  * first_name: string
  * last_name: string
  * phone: string
  * created_at: timestamp
  * updated_at: timestamp
}

table(houses) {
  + id: UUID <<PK>>
  --
  * user_id: UUID <<FK>>
  * name: string
  * address: string
  * timezone: string
  * created_at: timestamp
  * updated_at: timestamp
}

table(modules) {
  + id: UUID <<PK>>
  --
  * name: string
  * description: text
  * price: decimal
  * is_active: boolean
  * requirements: jsonb
}

table(user_modules) {
  + user_id: UUID <<PK>> <<FK>>
  + module_id: UUID <<PK>> <<FK>>
  --
  * purchased_at: timestamp
  * is_active: boolean
}

table(device_types) {
  + id: UUID <<PK>>
  --
  * name: string
  * category: string
  * description: text
  * supported_protocols: string[]
  * drivers: string[]
}

table(devices) {
  + id: UUID <<PK>>
  --
  * house_id: UUID <<FK>>
  * device_type_id: UUID <<FK>>
  * name: string
  * location: string
  * protocol: string
  * driver: string
  * status: string
  * created_at: timestamp
  * updated_at: timestamp
}

table(telemetry_data) {
  + id: UUID <<PK>>
  --
  * device_id: UUID <<FK>>
  * value: decimal
  * unit: string
  * created_at: timestamp
}

' === СВЯЗИ ===

' Один-ко-многим
users ||--o{ houses : "владеет"
houses ||--o{ devices : "содержит"
device_types ||--o{ devices : "классифицирует"

' Многие-ко-многим
users }o--|| user_modules : "приобретает"
modules }o--|| user_modules : "включается_в"

' Один-ко-многим (телеметрия)
devices ||--o{ telemetry_data : "генерирует"

@enduml