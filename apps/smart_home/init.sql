-- Create the database if it doesn't exist
--CREATE DATABASE smarthome;

-- Connect to the database
--\c smarthome;

-- Create the sensors table
CREATE TABLE IF NOT EXISTS sensors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    location VARCHAR(100) NOT NULL,
    value FLOAT DEFAULT 0,
    unit VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'inactive',
    last_updated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_sensors_type ON sensors(type);
CREATE INDEX IF NOT EXISTS idx_sensors_location ON sensors(location);
CREATE INDEX IF NOT EXISTS idx_sensors_status ON sensors(status);

-- New init
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS houses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    timezone VARCHAR(50) DEFAULT 'Europe/Moscow',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    house_id UUID REFERENCES houses(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    driver VARCHAR(50) NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    sensor_id VARCHAR(50),
    location VARCHAR(100),
    protocol VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS telemetry (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(20),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов
CREATE INDEX IF NOT EXISTS idx_devices_house_id ON devices(house_id);
CREATE INDEX IF NOT EXISTS idx_devices_sensor_id ON devices(sensor_id);
CREATE INDEX IF NOT EXISTS idx_telemetry_device_id ON telemetry(device_id);
CREATE INDEX IF NOT EXISTS idx_telemetry_metric_type ON telemetry(metric_type);
CREATE INDEX IF NOT EXISTS idx_telemetry_created_at ON telemetry(created_at);

-- Вставка тестовых данных
INSERT INTO users (id, email, password_hash, first_name, last_name) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'user@example.com', 'hashed_password', 'John', 'Doe')
ON CONFLICT (email) DO NOTHING;

INSERT INTO houses (id, user_id, name, address) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'My Smart Home', '123 Main St, Moscow')
ON CONFLICT (id) DO NOTHING;

INSERT INTO devices (id, house_id, name, device_type, sensor_id, location, protocol) VALUES
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Living Room Sensor', 'temperature', '1', 'Living Room', 'http'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Bedroom Sensor', 'temperature', '2', 'Bedroom', 'http'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Kitchen Sensor', 'temperature', '3', 'Kitchen', 'http')
ON CONFLICT (id) DO NOTHING;

INSERT INTO telemetry (device_id, metric_type, value, unit, metadata) VALUES
('bb481bfb-8287-4865-b915-93c8ffe9020a', 'temperature', 22.5, 'celsius', '{"source": "test"}'),
('bb481bfb-8287-4865-b915-93c8ffe9020a', 'brightness', 75, 'percent', '{"action": "manual"}')
ON CONFLICT DO NOTHING;