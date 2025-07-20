from flask import Flask, request, jsonify
import random

app = Flask(__name__)

@app.route('/temperature/<sensorID>', methods=['GET'])
def get_temperature(sensorID):
    location = request.args.get('location', '')
    if not location:
        if not sensorID:
             return jsonify({"error": "Location or sensorID is required"}), 400
        elif sensorID == "1":
            location = "Living Room"
        elif sensorID ==  "2":
            location = "Bedroom"
        elif sensorID ==  "3":
            location = "Kitchen"
        else :
            location = "Unknown"
    elif not sensorID:
        if not location:
           return jsonify({"error": "Location or sensorID is required"}), 400
        elif location == "Living Room":
            sensorID = "1"
        elif location ==  "Bedroom":
            sensorID = "2"
        elif location ==  "Kitchen":
            sensorID = "3"
        else:
            sensorID = "0"

    # Генерируем случайную температуру от 20 до 30 градусов
    temperature = round(random.uniform(20, 30), 1)

    response = {
        "status": "ok",
        "value": temperature,
        "location": location,
        "sensorID": sensorID
    }

    return jsonify(response)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8081, debug=True)