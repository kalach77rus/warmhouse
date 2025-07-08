#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import random
import secrets
import logging
from typing import Union

from flask import Flask, request
from flask_sslify import SSLify


class TemperatureApiApp:
    http_server_app = Flask("TemperatureApiApp")
    http_server_app_ssl = SSLify(http_server_app)
    http_server_app.secret_key = secrets.token_hex()

    def __init__(self):
        self.http_server_app.logger.setLevel(logging.INFO)
        self.setup_routes()

    def setup_routes(self):
        self.http_server_app.add_url_rule(
            "/temperature", methods=["GET"], view_func=self.temperature
        )

    def run(self, host: str, port: int, ssl_context: Union[str, None] = None):
        self.http_server_app.run(host=host, port=port, ssl_context=ssl_context)

    def temperature(self):
        location = request.args.get("location", "", type=str)
        sensorID = 0

        # If no location is provided, use a default based on sensor ID
        if location == "":
            if sensorID == 1:
                location = "Living Room"
            elif sensorID == 2:
                location = "Bedroom"
            elif sensorID == 3:
                location = "Kitchen"
            else:
                location = "Unknown"

        # If no sensor ID is provided, generate one based on location
        if sensorID == "":
            if location == "Living Room":
                sensorID = 1
            elif location == "Bedroom":
                sensorID = 2
            elif location == "Kitchen":
                sensorID = 3
            else:
                sensorID = 0

        result = random.randint(-30, 50)
        self.http_server_app.logger.info(
            "Get Temperature result: %d. Location: %s; sensorID: %d.",
            result,
            location,
            sensorID,
        )
        return json.dumps({"temperature": result}), 200


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        prog="server_test.py",
        description="""
        Test server for TemperatureApiApp. Request examples:
            curl --insecure -X GET  https://127.0.0.1:8081/temperature
        """,
        epilog="Example: python3 server.py -p 8081",
    )
    parser.add_argument(
        "-p",
        "--port",
        type=int,
        required=False,
        default=8081,
        help="Port for HTTPS server",
    )
    args = parser.parse_args()

    TemperatureApiApp().run("0.0.0.0", args.port, ssl_context="adhoc")
