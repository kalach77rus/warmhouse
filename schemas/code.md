@startuml
title C4 — Уровень кода (Code) Device Manager

class DeviceManager {
  - deviceRepository: DeviceRepository
  - stateManager: DeviceStateManager
  - commandProcessor: CommandProcessor
  + registerDevice(userId, name, type): Device
  + removeDevice(deviceId): bool
  + sendCommand(deviceId, action, value): bool
  + getDeviceStatus(deviceId): DeviceStatus
  + listUserDevices(userId): Device[]
}

class Device {
  - id: string
  - userId: string
  - name: string
  - type: string
  - status: string
  - configuration: map
  + updateConfig(key, value)
  + getStatus(): string
}

class DeviceRepository {
  - devices: map<string, Device>
  + save(device: Device)
  + findById(id: string): Device
  + findByUserId(userId: string): Device[]
  + delete(id: string)
}

class DeviceStateManager {
  - states: map<string, DeviceState>
  + getState(deviceId): DeviceState
  + updateState(deviceId, newState)
  + isDeviceOnline(deviceId): bool
}

class DeviceState {
  - deviceId: string
  - status: string
  - lastSeen: datetime
  - properties: map
}

class CommandProcessor {
  + processCommand(deviceId, action, value): bool
  + validateCommand(deviceId, action): bool
  + executeCommand(deviceId, action, value): bool
}

' Связи
DeviceManager --> DeviceRepository
DeviceManager --> DeviceStateManager
DeviceManager --> CommandProcessor

DeviceRepository --> Device
DeviceStateManager --> DeviceState
CommandProcessor --> Device

@enduml