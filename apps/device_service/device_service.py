from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Dict
import os, json
from kafka import KafkaProducer

app = FastAPI()

# Хранилище устройств (в памяти)
devices: Dict[str, dict] = {}

# Kafka producer
broker = os.getenv("KAFKA_BROKER", "kafka:9092")
producer = KafkaProducer(
    bootstrap_servers=broker,
    key_serializer=str.encode,
    value_serializer=lambda v: json.dumps(v).encode("utf-8")
)

# Модель устройства
class Device(BaseModel):
    id: str
    name: str
    typeId: str

# CRUD Endpoints
@app.get("/api/v1/devices")
def list_devices():
    return list(devices.values())

@app.post("/api/v1/devices")
def create_device(device: Device):
    if device.id in devices:
        raise HTTPException(status_code=400, detail="Device already exists")
    devices[device.id] = device.dict()
    # Публикуем событие device.created в Kafka
    producer.send("device-events", key="device.created", value=device.dict())
    return device

@app.get("/api/v1/devices/{device_id}")
def get_device(device_id: str):
    if device_id not in devices:
        raise HTTPException(status_code=404, detail="Device not found")
    return devices[device_id]

@app.delete("/api/v1/devices/{device_id}")
def delete_device(device_id: str):
    if device_id not in devices:
        raise HTTPException(status_code=404, detail="Device not found")
    del devices[device_id]
    # Публикуем событие device.deleted
    producer.send("device-events", key="device.deleted", value={"id": device_id})
    return {"status": "deleted"}
