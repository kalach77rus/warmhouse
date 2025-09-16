package models

import (
	"math"
	"math/rand"
	"time"
)

type TemperatureResponse struct {
	Value       float64   `json:"value"`
	Unit        string    `json:"unit"`
	Timestamp   time.Time `json:"timestamp"`
	Location    string    `json:"location"`
	Status      string    `json:"status"`
	SensorID    string    `json:"sensor_id"`
	SensorType  string    `json:"sensor_type"`
	Description string    `json:"description"`
}

func (t *TemperatureResponse) SetRandomTemp(minTemp, maxTemp *float64) {
	var minVal = -10.0
	var maxVal = 45.0

	if minTemp != nil {
		minVal = *minTemp
	}

	if maxTemp != nil {
		maxVal = *maxTemp
	}

	rand.New(rand.NewSource(time.Now().UnixNano()))
	randomFloat := rand.Float64()

	value := randomFloat*maxVal + minVal
	t.Value = math.Round(value*100) / 100
	t.Unit = "°C"
	t.Timestamp = time.Now()
	t.Status = "active"
}
