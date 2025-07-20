from flask import Flask, request, jsonify
import random

app = Flask(__name__)

@app.route('/temperature', methods=['GET'])
def get_temperature():
    location = request.args.get('location', '')
    if not location:
        return jsonify({"error": "Location parameter is required"}), 400

    # Генерируем случайную температуру от -30 до +40 градусов
    return round(random.uniform(-30, 40), 1)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8081, debug=True)