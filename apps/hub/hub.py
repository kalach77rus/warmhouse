import time
import random
import json
from datetime import datetime
import requests
from kafka import KafkaProducer
from threading import Thread, Event

class TemperatureDataProducer:
    def __init__(self, bootstrap_servers, topic_name, api_url):
        self.bootstrap_servers = bootstrap_servers
        self.topic_name = topic_name
        self.api_url = api_url
        self.devices = []
        self.producer = None
        self.stop_event = Event()

    def fetch_devices(self):
        try:
            response = requests.get(self.api_url)
            response.raise_for_status()
            self.devices = response.json()
            print(f"Fetched {len(self.devices)} devices at {datetime.now()}")
        except Exception as e:
            print(f"Error fetching devices: {e}")
            # Keep using previous device list if available

    def create_producer(self):
        try:
            self.producer = KafkaProducer(
                bootstrap_servers=self.bootstrap_servers,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                acks='all',
                retries=3
            )
            print("Kafka producer created")
        except Exception as e:
            print(f"Error creating Kafka producer: {e}")
            raise

    def generate_temperature_data(self, device_id):
        return {
            "value": round(random.uniform(20, 30), 2),
            "time": datetime.now().isoformat(),
            "deviceId": device_id
        }

    def send_data(self):
        if not self.devices:
            print("No devices available to send data")
            return

        for device in self.devices:
            if device['type'] == 'temperature':
                continue
            try:
                device_id = device['id']
                data = self.generate_temperature_data(device_id)
                self.producer.send(self.topic_name, value=data)
                print(f"Sent data for device {device_id}: {data}")
            except Exception as e:
                print(f"Error sending data for device {device_id}: {e}")

    def start_data_producer(self, data_interval=5, refresh_interval=30):
        self.create_producer()
        self.fetch_devices()

        last_refresh = time.time()

        while not self.stop_event.is_set():
            current_time = time.time()

            if current_time - last_refresh >= refresh_interval:
                self.fetch_devices()
                print("Device list refreshed")
                last_refresh = current_time

            self.send_data()

            self.stop_event.wait(data_interval)

    def stop(self):
        self.stop_event.set()
        if self.producer:
            self.producer.close()
        print("Producer stopped")

if __name__ == "__main__":
    KAFKA_BOOTSTRAP_SERVERS = 'kafka:9092'
    KAFKA_TOPIC = 'telemetry-data'
    API_URL = 'http://app:8080/api/v1/sensors'

    producer = TemperatureDataProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        topic_name=KAFKA_TOPIC,
        api_url=API_URL
    )

    try:
        print("Starting temperature data producer...")
        producer_thread = Thread(target=producer.start_data_producer)
        producer_thread.start()

        while True:
            time.sleep(1)

    except KeyboardInterrupt:
        print("Stopping producer...")
        producer.stop()
        producer_thread.join()
        print("Exited cleanly")