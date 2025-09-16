FROM python:latest
WORKDIR /app

# Устанавливаем зависимости
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Копируем код
COPY . .

EXPOSE 8080
CMD ["uvicorn", "device_service:app", "--host", "0.0.0.0", "--port", "8080"]
