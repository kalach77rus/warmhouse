#!/bin/bash

echo "🧹 Очистка Docker окружения для проекта WarmHouse"

echo "1. Остановка и удаление контейнеров с volumes..."
docker-compose down -v

echo "2. Удаление неиспользуемых volumes..."
docker volume prune -f

echo "3. Удаление неиспользуемых networks..."
docker network prune -f

echo "4. Удаление неиспользуемых images (опционально)..."
read -p "Удалить неиспользуемые Docker images? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker image prune -f
fi

echo "✅ Очистка завершена!"
echo "Теперь можно запустить: docker-compose up --build"
