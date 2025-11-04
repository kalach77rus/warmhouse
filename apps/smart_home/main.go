package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"smarthome/db"
	"smarthome/handlers"
	"smarthome/services"

	"github.com/gin-gonic/gin"
)

func main() {
	// Set up database connection
	dbURL := getEnv("DATABASE_URL", "postgres://postgres:postgres@localhost:5432/smarthome")
	database, err := db.New(dbURL)
	if err != nil {
		log.Fatalf("Unable to connect to database: %v\n", err)
	}
	defer database.Close()

	log.Println("Connected to database successfully")

	// Initialize external services
	temperatureAPIURL := getEnv("TEMPERATURE_API_URL", "http://temperature-api:8081")
	deviceServiceURL := getEnv("DEVICE_SERVICE_URL", "http://device-service:8082")
	lampServiceURL := getEnv("LAMP_SERVICE_URL", "http://lamp-service:8083")
	telemetryServiceURL := getEnv("TELEMETRY_SERVICE_URL", "http://telemetry-service:8084")

	// Initialize services
	temperatureService := services.NewTemperatureService(temperatureAPIURL)
	deviceService := services.NewDeviceService(deviceServiceURL)
	lampService := services.NewLampService(lampServiceURL)
	telemetryService := services.NewTelemetryService(telemetryServiceURL)

	log.Printf("External services initialized:")
	log.Printf("  Temperature API: %s\n", temperatureAPIURL)
	log.Printf("  Device Service: %s\n", deviceServiceURL)
	log.Printf("  Lamp Service: %s\n", lampServiceURL)
	log.Printf("  Telemetry Service: %s\n", telemetryServiceURL)

	// Initialize router
	router := gin.Default()

	// Health check endpoint
	router.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"status": "ok",
			"services": gin.H{
				"database":         "connected",
				"temperature_api":  "available",
				"device_service":   "available", 
				"lamp_service":     "available",
				"telemetry_service": "available",
			},
		})
	})

	// API routes
	apiRoutes := router.Group("/api/v1")

	// Register sensor routes (legacy)
	sensorHandler := handlers.NewSensorHandler(database, temperatureService)
	sensorHandler.RegisterRoutes(apiRoutes)

	// NEW: Register device routes that use microservices
	deviceHandler := handlers.NewDeviceHandler(deviceService, lampService, telemetryService)
	deviceHandler.RegisterRoutes(apiRoutes)

	// Start server
	srv := &http.Server{
		Addr:    getEnv("PORT", ":8080"),
		Handler: router,
	}

	// Start the server in a goroutine
	go func() {
		log.Printf("Server starting on %s\n", srv.Addr)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Failed to start server: %v\n", err)
		}
	}()

	// Wait for interrupt signal to gracefully shut down the server
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down server...")

	// Create a deadline for server shutdown
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("Server forced to shutdown: %v\n", err)
	}

	log.Println("Server exited properly")
}

// getEnv gets an environment variable or returns a default value
func getEnv(key, defaultValue string) string {
	value := os.Getenv(key)
	if value == "" {
		return defaultValue
	}
	return value
}