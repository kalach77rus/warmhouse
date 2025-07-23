import http.client
import json
from kafka import KafkaConsumer

KAFKA_BROKERS = 'kafka:9092'
TOPIC_NAME = 'telemetry-data'
GROUP_ID = 'telemetry-consumer-group'


def create_consumer():
    return KafkaConsumer(
        TOPIC_NAME,
        bootstrap_servers=KAFKA_BROKERS,
        auto_offset_reset='earliest',
        group_id=GROUP_ID,
        value_deserializer=lambda m: json.loads(m.decode('utf-8')))

def process_message(message):
    try:
        value = message['value']
        deviceId = message['deviceId']

        host = "app:8080"
        conn = http.client.HTTPConnection(host)
        path = f"/api/v1/sensors/{deviceId}/value"
        params = json.dumps({"value": value,
                             "status": 'ok'
                             })
        conn.request("PATCH", path, params)
        response = conn.getresponse()
        print(response.status, response.reason)
    except Exception as e:
        print(f"Error processing message: {message}. Error: {str(e)}")

def consume_messages():
    consumer = create_consumer()
    print(f"Starting to consume messages from topic '{TOPIC_NAME}'...")

    try:
        for msg in consumer:
            process_message(msg.value)
    except KeyboardInterrupt:
        print("Stopping consumer...")
    finally:
        consumer.close()

if __name__ == '__main__':
    consume_messages()
