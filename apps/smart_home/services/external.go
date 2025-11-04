package services

import (
	"bytes"  // Добавь этот импорт
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

// Удали ненужный импорт "io"

// DeviceService клиент для микросервиса устройств
type DeviceService struct {
	baseURL string
	client  *http.Client
}

func NewDeviceService(baseURL string) *DeviceService {
	return &DeviceService{
		baseURL: baseURL,
		client: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

func (s *DeviceService) GetDevices(deviceType string) ([]Device, error) {
	url := fmt.Sprintf("%s/devices", s.baseURL)
	if deviceType != "" {
		url = fmt.Sprintf("%s?device_type=%s", url, deviceType)
	}

	resp, err := s.client.Get(url)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	var result struct {
		Success bool `json:"success"`
		Data    struct {
			Devices []Device `json:"devices"`
		} `json:"data"`
	}

	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, err
	}

	return result.Data.Devices, nil
}

type Device struct {
	ID         string `json:"id"`
	Name       string `json:"name"`
	DeviceType string `json:"device_type"`
	Location   string `json:"location"`
	Status     string `json:"status"`
}

// LampService клиент для микросервиса ламп
type LampService struct {
	baseURL string
	client  *http.Client
}

func NewLampService(baseURL string) *LampService {
	return &LampService{
		baseURL: baseURL,
		client: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

func (s *LampService) GetLamps() ([]Lamp, error) {
	resp, err := s.client.Get(s.baseURL + "/lamps")
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	var result struct {
		Success bool `json:"success"`
		Data    struct {
			Lamps []Lamp `json:"lamps"`
		} `json:"data"`
	}

	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, err
	}

	return result.Data.Lamps, nil
}

type Lamp struct {
	ID    string                 `json:"id"`
	Name  string                 `json:"name"`
	State map[string]interface{} `json:"state"`
}

// TelemetryService клиент для микросервиса телеметрии
type TelemetryService struct {
	baseURL string
	client  *http.Client
}

func NewTelemetryService(baseURL string) *TelemetryService {
	return &TelemetryService{
		baseURL: baseURL,
		client: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

func (s *TelemetryService) SendTelemetry(data TelemetryData) error {
	jsonData, err := json.Marshal(data)
	if err != nil {
		return err
	}

	resp, err := s.client.Post(s.baseURL+"/telemetry", "application/json", bytes.NewReader(jsonData))
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	return nil
}

type TelemetryData struct {
	DeviceID   string                 `json:"device_id"`
	MetricType string                 `json:"metric_type"`
	Value      float64                `json:"value"`
	Unit       string                 `json:"unit"`
	Metadata   map[string]interface{} `json:"metadata"`
}