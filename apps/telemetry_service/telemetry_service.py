from fastapi import FastAPI
from pydantic import BaseModel
from typing import Dict
import os, json
from kafka import KafkaConsumer

app = FastAPI()
telemetry_data: Dict[str, list] = {}

class Telemetry(BaseModel):
    device_id: str
    temperature: float
    humidity: float

@app.post("/api/v1/telemetry")
def ingest_telemetry(data: Telemetry):
    if data.device_id not in telemetry_data:
        telemetry_data[data.device_id] = []
    telemetry_data[data.device_id].append(data.dict())
    return {"status": "accepted"}

@app.get("/api/v1/telemetry/{device_id}")
def get_telemetry(device_id: str):
    return telemetry_data.get(device_id, [])

# Kafka consumer для событий device.created
def consume_kafka():
    broker = os.getenv("KAFKA_BROKER", "kafka:9092")
    consumer = KafkaConsumer(
        'device-events',
        bootstrap_servers=broker,
        group_id='telemetry-group',
        value_deserializer=lambda m: json.loads(m.decode('utf-8'))
    )
    for message in consumer:
        print("Received event:", message.key, message.value)

import threading
threading.Thread(target=consume_kafka, daemon=True).start()
