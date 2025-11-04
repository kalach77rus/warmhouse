from fastapi import FastAPI, HTTPException, Depends
from pydantic import BaseModel
import psycopg2
import requests
import os
import uuid
from typing import List, Optional, Dict, Any
from datetime import datetime

app = FastAPI(
    title="API «Тёплый дом»",
    description="Микросервис Device Manager",
    version="1.0.0"
)

# Конфигурация
DATABASE_URL = os.getenv("DATABASE_URL")
TELEMETRY_SERVICE_URL = os.getenv("TELEMETRY_SERVICE_URL", "http://telemetry-service:8084")
LAMP_SERVICE_URL = os.getenv("LAMP_SERVICE_URL", "http://lamp-service:8083")
TEMPERATURE_API_URL = os.getenv("TEMPERATURE_API_URL", "http://temperature-api:8081")

# Модели данных в соответствии с OpenAPI
class DeviceRegistrationRequest(BaseModel):
    name: str
    device_type: str
    house_id: str
    protocol: str
    driver: str
    location: Optional[str] = None

class DeviceUpdateRequest(BaseModel):
    name: Optional[str] = None
    location: Optional[str] = None
    status: Optional[str] = None

class DeviceCommandRequest(BaseModel):
    action: str
    parameters: Optional[Dict[str, Any]] = None
    priority: Optional[str] = "normal"

class DeviceResponse(BaseModel):
    success: bool
    data: Dict[str, Any]

class DeviceListResponse(BaseModel):
    success: bool
    data: Dict[str, List[Dict]]

class CommandResponse(BaseModel):
    success: bool
    data: Dict[str, Any]

# Подключение к БД
def get_db_connection():
    return psycopg2.connect(DATABASE_URL)

def generate_uuid():
    return str(uuid.uuid4())

@app.get("/health")
async def health():
    return {"status": "healthy", "service": "device-service"}

@app.get("/devices", response_model=DeviceListResponse)
async def get_devices(house_id: Optional[str] = None, device_type: Optional[str] = None):
    """Получить список устройств пользователя в доме"""
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        
        query = """
            SELECT id, name, device_type, house_id, protocol, driver, status, location, created_at, updated_at
            FROM devices
            WHERE 1=1
        """
        params = []
        
        if house_id:
            query += " AND house_id = %s"
            params.append(house_id)
        
        if device_type:
            query += " AND device_type = %s"
            params.append(device_type)
            
        query += " ORDER BY created_at DESC"
        
        cur.execute(query, params)
        devices = []
        
        for row in cur.fetchall():
            device = {
                "id": row[0],
                "name": row[1],
                "device_type": row[2],
                "house_id": row[3],
                "protocol": row[4],
                "driver": row[5],
                "status": row[6],
                "location": row[7],
                "created_at": row[8].isoformat() if row[8] else None,
                "updated_at": row[9].isoformat() if row[9] else None
            }
            
            # Обогащаем данными из других сервисов
            if device["device_type"] == "light":
                device = await _enrich_with_lamp_data(device)
            elif device["device_type"] == "thermostat":
                device = await _enrich_with_temperature_data(device)
                
            devices.append(device)
        
        return DeviceListResponse(
            success=True,
            data={"devices": devices}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()

@app.post("/devices", response_model=DeviceResponse)
async def create_device(device_data: DeviceRegistrationRequest):
    """Зарегистрировать новое устройство"""
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        
        device_id = generate_uuid()
        now = datetime.now()
        
        cur.execute("""
            INSERT INTO devices (id, name, device_type, house_id, protocol, driver, location, status, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, 'active', %s, %s)
            RETURNING id, name, device_type, house_id, protocol, driver, location, status, created_at, updated_at
        """, (
            device_id, device_data.name, device_data.device_type, 
            device_data.house_id, device_data.protocol, device_data.driver,
            device_data.location, now, now
        ))
        
        result = cur.fetchone()
        conn.commit()
        
        device = {
            "id": result[0],
            "name": result[1],
            "device_type": result[2],
            "house_id": result[3],
            "protocol": result[4],
            "driver": result[5],
            "location": result[6],
            "status": result[7],
            "created_at": result[8].isoformat(),
            "updated_at": result[9].isoformat()
        }
        
        # Отправляем телеметрию о создании устройства
        await _send_telemetry(device_id, "device_registered", 1, "count", {})
        
        return DeviceResponse(
            success=True,
            data={"device": device}
        )
    except Exception as e:
        conn.rollback()
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()

@app.get("/devices/{device_id}", response_model=DeviceResponse)
async def get_device(device_id: str):
    """Получить информацию об устройстве"""
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        
        cur.execute("""
            SELECT id, name, device_type, house_id, protocol, driver, status, location, created_at, updated_at
            FROM devices WHERE id = %s
        """, (device_id,))
        
        result = cur.fetchone()
        if not result:
            raise HTTPException(status_code=404, detail="Device not found")
        
        device = {
            "id": result[0],
            "name": result[1],
            "device_type": result[2],
            "house_id": result[3],
            "protocol": result[4],
            "driver": result[5],
            "status": result[6],
            "location": result[7],
            "created_at": result[8].isoformat() if result[8] else None,
            "updated_at": result[9].isoformat() if result[9] else None
        }
        
        # Обогащаем данными из других сервисов
        if device["device_type"] == "light":
            device = await _enrich_with_lamp_data(device)
        elif device["device_type"] == "thermostat":
            device = await _enrich_with_temperature_data(device)
        
        return DeviceResponse(
            success=True,
            data={"device": device}
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()

@app.put("/devices/{device_id}", response_model=DeviceResponse)
async def update_device(device_id: str, device_data: DeviceUpdateRequest):
    """Обновить информацию об устройстве"""
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        
        # Проверяем существование устройства
        cur.execute("SELECT id FROM devices WHERE id = %s", (device_id,))
        if not cur.fetchone():
            raise HTTPException(status_code=404, detail="Device not found")
        
        # Строим динамический UPDATE запрос
        update_fields = []
        params = []
        
        if device_data.name is not None:
            update_fields.append("name = %s")
            params.append(device_data.name)
        if device_data.location is not None:
            update_fields.append("location = %s")
            params.append(device_data.location)
        if device_data.status is not None:
            update_fields.append("status = %s")
            params.append(device_data.status)
            
        if not update_fields:
            raise HTTPException(status_code=400, detail="No fields to update")
            
        update_fields.append("updated_at = %s")
        params.append(datetime.now())
        params.append(device_id)
        
        query = f"UPDATE devices SET {', '.join(update_fields)} WHERE id = %s RETURNING *"
        cur.execute(query, params)
        
        result = cur.fetchone()
        conn.commit()
        
        device = {
            "id": result[0],
            "name": result[1],
            "device_type": result[2],
            "house_id": result[3],
            "protocol": result[4],
            "driver": result[5],
            "status": result[6],
            "location": result[7],
            "created_at": result[8].isoformat(),
            "updated_at": result[9].isoformat()
        }
        
        return DeviceResponse(
            success=True,
            data={"device": device}
        )
    except HTTPException:
        raise
    except Exception as e:
        conn.rollback()
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()

@app.delete("/devices/{device_id}")
async def delete_device(device_id: str):
    """Удалить устройство"""
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        
        cur.execute("DELETE FROM devices WHERE id = %s", (device_id,))
        if cur.rowcount == 0:
            raise HTTPException(status_code=404, detail="Device not found")
            
        conn.commit()
        
        # Отправляем телеметрию об удалении устройства
        await _send_telemetry(device_id, "device_deleted", 1, "count", {})
        
        return {"success": True}, 204
    except HTTPException:
        raise
    except Exception as e:
        conn.rollback()
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()

@app.post("/devices/{device_id}/commands", response_model=CommandResponse)
async def send_command(device_id: str, command: DeviceCommandRequest):
    """Отправить команду устройству"""
    try:
        # Получаем информацию об устройстве
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT device_type, status FROM devices WHERE id = %s", (device_id,))
        result = cur.fetchone()
        conn.close()
        
        if not result:
            raise HTTPException(status_code=404, detail="Device not found")
            
        device_type, status = result
        
        if status != "active":
            raise HTTPException(status_code=409, detail="Device is not active")
        
        # Маршрутизируем команду в соответствующий сервис
        if device_type == "light":
            response = await _send_lamp_command(device_id, command)
        elif device_type == "thermostat":
            response = await _send_temperature_command(device_id, command)
        else:
            # Общая команда для других устройств
            response = await _send_generic_command(device_id, command)
        
        # Отправляем телеметрию о выполнении команды
        await _send_telemetry(
            device_id, 
            "command_executed", 
            1, 
            "count",
            {"action": command.action, "parameters": command.parameters}
        )
        
        return CommandResponse(
            success=True,
            data={
                "command_id": generate_uuid(),
                "status": "accepted",
                "message": "Команда принята к выполнению"
            }
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Вспомогательные функции для интеграции с другими сервисами
async def _enrich_with_lamp_data(device: Dict) -> Dict:
    """Обогащает данные устройства информацией из lamp-service"""
    try:
        response = requests.get(f"{LAMP_SERVICE_URL}/lamps", timeout=5)
        if response.status_code == 200:
            lamps_data = response.json()
            for lamp in lamps_data.get("data", {}).get("lamps", []):
                if lamp.get("id") == device["id"]:
                    device["lamp_state"] = lamp.get("state", {})
                    break
    except requests.exceptions.RequestException:
        # Продолжаем без данных от lamp-service
        pass
    return device

async def _enrich_with_temperature_data(device: Dict) -> Dict:
    """Обогащает данные устройства информацией из temperature-api"""
    try:
        response = requests.get(
            f"{TEMPERATURE_API_URL}/temperature",
            params={"location": device["location"]},
            timeout=5
        )
        if response.status_code == 200:
            temp_data = response.json()
            device["current_temperature"] = temp_data.get("data", {}).get("temperature")
    except requests.exceptions.RequestException:
        # Продолжаем без данных от temperature-api
        pass
    return device

async def _send_lamp_command(device_id: str, command: DeviceCommandRequest):
    """Отправляет команду в lamp-service"""
    try:
        if command.action == "turn_on":
            response = requests.post(f"{LAMP_SERVICE_URL}/lamps/{device_id}/toggle", timeout=10)
        elif command.action == "set_brightness" and command.parameters:
            response = requests.post(
                f"{LAMP_SERVICE_URL}/lamps/{device_id}/brightness",
                json={"brightness": command.parameters.get("brightness", 100)},
                timeout=10
            )
        elif command.action == "set_color" and command.parameters:
            response = requests.post(
                f"{LAMP_SERVICE_URL}/lamps/{device_id}/color",
                json=command.parameters,
                timeout=10
            )
        else:
            raise HTTPException(status_code=422, detail="Unsupported command for lamp")
            
        if response.status_code != 200:
            raise HTTPException(status_code=500, detail="Failed to execute lamp command")
    except requests.exceptions.RequestException:
        raise HTTPException(status_code=500, detail="Lamp service unavailable")

async def _send_temperature_command(device_id: str, command: DeviceCommandRequest):
    """Отправляет команду для термостата"""
    # Для термостатов можем использовать temperature-api или другую логику
    if command.action == "set_temperature" and command.parameters:
        # Здесь может быть логика управления термостатом
        pass
    else:
        raise HTTPException(status_code=422, detail="Unsupported command for thermostat")

async def _send_generic_command(device_id: str, command: DeviceCommandRequest):
    """Отправляет общую команду устройству"""
    # Базовая логика для других типов устройств
    pass

async def _send_telemetry(device_id: str, metric_type: str, value: float, unit: str, metadata: Dict = None):
    """Отправляет данные телеметрии"""
    try:
        requests.post(
            f"{TELEMETRY_SERVICE_URL}/telemetry",
            json={
                "device_id": device_id,
                "metric_type": metric_type,
                "value": value,
                "unit": unit,
            },
            timeout=3
        )
    except requests.exceptions.RequestException:
        # Логируем ошибку, но не прерываем выполнение
        print(f"Failed to send telemetry for device {device_id}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8082)