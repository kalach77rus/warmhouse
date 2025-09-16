package handlers

import (
	"net/http"
	"strconv"
	"temp_api/models"

	"github.com/gin-gonic/gin"
)

func TempByLocation(c *gin.Context) {
	location := c.Query("location")

	tempVal := models.TemperatureResponse{
		Location: location,
	}

	tempVal.SetRandomTemp(nil, nil)

	switch location {
	case "Living Room":
		tempVal.SensorID = "1"
	case "Bedroom":
		tempVal.SensorID = "2"
	case "Kitchen":
		tempVal.SensorID = "3"
	default:
		tempVal.SensorID = "0"
	}

	c.JSON(http.StatusOK, tempVal)
}

func TempBySensor(c *gin.Context) {
	sensorId, err := strconv.Atoi(c.Param("sensorId"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid sensor ID"})
		return
	}

	tempVal := models.TemperatureResponse{
		SensorID: strconv.Itoa(sensorId),
	}
	tempVal.SetRandomTemp(nil, nil)

	switch tempVal.SensorID {
	case "1":
		tempVal.Location = "Living Room"
	case "2":
		tempVal.Location = "Bedroom"
	case "3":
		tempVal.Location = "Kitchen"
	default:
		tempVal.Location = "Unknown"
	}

	c.JSON(http.StatusOK, tempVal)
}
