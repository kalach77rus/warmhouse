const express = require('express');
const { Client } = require('pg');

const app = express();
app.use(express.json());

const PORT = process.env.PORT || 8084;
const DATABASE_URL = process.env.DATABASE_URL || 'postgres://postgres:postgres@postgres:5432/smarthome';

// Подключение к БД
const client = new Client({
    connectionString: DATABASE_URL
});

client.connect().then(() => {
    console.log('Connected to PostgreSQL database');
}).catch(err => {
    console.error('Database connection error:', err);
});

// Health check
app.get('/health', (req, res) => {
    res.json({
        status: 'healthy',
        service: 'telemetry-service',
        timestamp: new Date().toISOString(),
        database: client._connected ? 'connected' : 'disconnected'
    });
});

// Получить телеметрию
app.get('/telemetry', async (req, res) => {
    const { device_id, metric_type, limit = 100 } = req.query;
    
    try {
        let query = 'SELECT * FROM telemetry';
        const params = [];
        let paramCount = 0;
        
        if (device_id || metric_type) {
            query += ' WHERE';
            if (device_id) {
                query += ` device_id = $${++paramCount}`;
                params.push(device_id);
            }
            if (metric_type) {
                if (device_id) query += ' AND';
                query += ` metric_type = $${++paramCount}`;
                params.push(metric_type);
            }
        }
        
        query += ' ORDER BY created_at DESC LIMIT $' + (++paramCount);
        params.push(parseInt(limit));
        
        const result = await client.query(query, params);
        
        res.json({
            success: true,
            data: {
                telemetry: result.rows
            }
        });
    } catch (error) {
        console.error('Error fetching telemetry:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch telemetry data'
        });
    }
});

// Отправить телеметрию
app.post('/telemetry', async (req, res) => {
    const { device_id, metric_type, value, unit } = req.body;
    
    if (!device_id || !metric_type || value === undefined) {
        return res.status(400).json({
            success: false,
            error: 'Missing required fields: device_id, metric_type, value'
        });
    }
    
    try {
        const query = `
            INSERT INTO telemetry (device_id, metric_type, value, unit, created_at)
            VALUES ($1, $2, $3, $4, $5, $6)
            RETURNING *
        `;
        
        const values = [
            device_id,
            metric_type,
            value,
            unit || null,
            new Date()
        ];
        
        const result = await client.query(query, values);
        
        res.json({
            success: true,
            data: {
                telemetry: result.rows[0],
                message: 'Telemetry data saved successfully'
            }
        });
    } catch (error) {
        console.error('Error saving telemetry:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to save telemetry data'
        });
    }
});

// Статистика по устройствам
app.get('/telemetry/stats', async (req, res) => {
    const { device_id } = req.query;
    
    try {
        let query = `
            SELECT 
                device_id,
                metric_type,
                COUNT(*) as count,
                AVG(value) as avg_value,
                MIN(value) as min_value,
                MAX(value) as max_value,
                MAX(created_at) as last_recorded
            FROM telemetry
        `;
        const params = [];
        
        if (device_id) {
            query += ' WHERE device_id = $1';
            params.push(device_id);
        }
        
        query += ' GROUP BY device_id, metric_type ORDER BY device_id, metric_type';
        
        const result = await client.query(query, params);
        
        res.json({
            success: true,
            data: {
                stats: result.rows
            }
        });
    } catch (error) {
        console.error('Error fetching stats:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch telemetry statistics'
        });
    }
});

app.listen(PORT, () => {
    console.log(`Telemetry Service running on port ${PORT}`);
});