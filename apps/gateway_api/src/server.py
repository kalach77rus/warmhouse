#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import random
import logging
from typing import Union

from flask import Flask, request
from flask_sslify import SSLify


class GatewayApiApp:
    http_server_app = Flask("GatewayApiApp")

    def __init__(self):
        self.http_server_app.logger.setLevel(logging.INFO)
        self.setup_routes()

    def setup_routes(self):
        self.http_server_app.add_url_rule(
            "/api/v1/sendRequest", methods=["POST"], view_func=self.proxy_request
        )

    def run(self, host: str, port: int, ssl_context: Union[str, None] = None):
        self.http_server_app.run(host=host, port=port, ssl_context=ssl_context)

    def proxy_request(self):
        # TODO: send request to relay with passed input_json
        # TODO: save response status to repository
        return json.dumps({"result": "OK"}), 200


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        prog="server_test.py",
        description="""
        Test server for GatewayApiApp. Request examples:
            curl --insecure -X GET  https://127.0.0.1:8082/temperature
        """,
        epilog="Example: python3 server.py -p 8082",
    )
    parser.add_argument(
        "-p",
        "--port",
        type=int,
        required=False,
        default=8082,
        help="Port for HTTPS server",
    )
    args = parser.parse_args()

    GatewayApiApp().run("0.0.0.0", args.port, ssl_context="adhoc")
