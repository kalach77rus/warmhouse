const express = require('express');
const axios = require('axios');

const app = express();
app.use(express.json());

const PORT = process.env.PORT || 8083;
const DEVICE_SERVICE_URL = process.env.DEVICE_SERVICE_URL || 'http://device-service:8082';
const TELEMETRY_SERVICE_URL = process.env.TELEMETRY_SERVICE_URL || 'http://telemetry-service:8084';

// Хранилище состояния ламп (в памяти)
const lamps = new Map();

// Health check
app.get('/health', (req, res) => {
    res.json({
        status: 'healthy',
        service: 'lamp-service',
        timestamp: new Date().toISOString()
    });
});

let cachedDevices = null;
let lastUpdate = 0;
const CACHE_TTL = 30000; // 30 секунд

app.get('/lamps', async (req, res) => {
    try {
        // Используем кэш если данные свежие
        if (cachedDevices && Date.now() - lastUpdate < CACHE_TTL) {
            return res.json({
                success: true,
                data: { lamps: cachedDevices },
                cached: true
            });
        }
        
        const response = await axios.get(`${DEVICE_SERVICE_URL}/devices?device_type=light`);
        const devices = response.data?.data?.devices || [];
        
        const lampsWithState = devices.map(device => ({
            ...device,
            state: lamps.get(device.id) || {
                power: 'off',
                brightness: 100,
                color: { r: 255, g: 255, b: 255 }
            }
        }));
        
        // Обновляем кэш
        cachedDevices = lampsWithState;
        lastUpdate = Date.now();
        
        res.json({
            success: true,
            data: { lamps: lampsWithState }
        });
        
    } catch (error) {
        // В случае ошибки используем кэшированные данные если есть
        if (cachedDevices) {
            return res.json({
                success: true,
                data: { lamps: cachedDevices },
                cached: true,
                error: 'Using cached data due to device service error'
            });
        }
        
        res.status(500).json({
            success: false,
            error: 'Failed to fetch lamps'
        });
    }
});

// Включить/выключить лампу
app.post('/lamps/:lampId/toggle', async (req, res) => {
    const { lampId } = req.params;
    
    try {
        const currentState = lamps.get(lampId) || { power: 'off', brightness: 100 };
        const newPower = currentState.power === 'on' ? 'off' : 'on';
        
        lamps.set(lampId, {
            ...currentState,
            power: newPower
        });
        
        // Отправляем телеметрию
        await axios.post(`${TELEMETRY_SERVICE_URL}/telemetry`, {
            device_id: lampId,
            metric_type: 'lamp_state',
            value: newPower === 'on' ? 1 : 0,
            unit: 'state',
        }).catch(err => console.error('Telemetry error:', err.message));
        
        res.json({
            success: true,
            data: {
                lamp_id: lampId,
                power: newPower,
                message: `Lamp turned ${newPower}`
            }
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            error: 'Failed to toggle lamp'
        });
    }
});

// Установить яркость
app.post('/lamps/:lampId/brightness', async (req, res) => {
    const { lampId } = req.params;
    const { brightness } = req.body;
    
    if (brightness < 0 || brightness > 100) {
        return res.status(400).json({
            success: false,
            error: 'Brightness must be between 0 and 100'
        });
    }
    
    try {
        const currentState = lamps.get(lampId) || { power: 'off', brightness: 100 };
        
        lamps.set(lampId, {
            ...currentState,
            brightness: brightness,
            power: brightness > 0 ? 'on' : 'off'
        });
        
        // Отправляем телеметрию
        await axios.post(`${TELEMETRY_SERVICE_URL}/telemetry`, {
            device_id: lampId,
            metric_type: 'brightness',
            value: brightness,
            unit: 'percent',
        }).catch(err => console.error('Telemetry error:', err.message));
        
        res.json({
            success: true,
            data: {
                lamp_id: lampId,
                brightness: brightness,
                power: brightness > 0 ? 'on' : 'off'
            }
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            error: 'Failed to set brightness'
        });
    }
});

app.listen(PORT, () => {
    console.log(`Lamp Service running on port ${PORT}`);
});