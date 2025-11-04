package handlers

import (
	"net/http"
	"smarthome/services"

	"github.com/gin-gonic/gin"
)

type DeviceHandler struct {
	deviceService   *services.DeviceService
	lampService     *services.LampService
	telemetryService *services.TelemetryService
}

func NewDeviceHandler(deviceService *services.DeviceService, lampService *services.LampService, telemetryService *services.TelemetryService) *DeviceHandler {
	return &DeviceHandler{
		deviceService:   deviceService,
		lampService:     lampService,
		telemetryService: telemetryService,
	}
}

func (h *DeviceHandler) RegisterRoutes(router *gin.RouterGroup) {
	devices := router.Group("/devices")
	{
		devices.GET("/", h.getDevices)
		devices.GET("/lamps", h.getLamps)
	}
}

func (h *DeviceHandler) getDevices(c *gin.Context) {
	deviceType := c.Query("device_type")
	
	devices, err := h.deviceService.GetDevices(deviceType)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to fetch devices from device service",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"success": true,
		"data": gin.H{
			"devices": devices,
		},
		"source": "device_service",
	})
}

func (h *DeviceHandler) getLamps(c *gin.Context) {
	lamps, err := h.lampService.GetLamps()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to fetch lamps from lamp service",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"success": true,
		"data": gin.H{
			"lamps": lamps,
		},
		"source": "lamp_service",
	})
}