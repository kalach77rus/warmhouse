# temperature_api.py
from fastapi import FastAPI, Query
from fastapi.responses import JSONResponse
import random

app = FastAPI(title="Temperature API", description="Simulates remote temperature sensor")

@app.get("/temperature")
def get_temperature(location: str = Query(..., description="Location name, e.g. kitchen")):
    temperature = round(random.uniform(16.0, 28.0), 1)  # Имитация температуры
    return JSONResponse(content={
        "location": location,
        "temperature": temperature,
        "unit": "°C"
    })
