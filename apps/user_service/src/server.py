#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import random
import logging
import secrets
from typing import Union

from flask import Flask, request
from flask_sslify import SSLify

from repository import Repository
from service import UserService


class UserServiceController:
    http_server_app = Flask("UserServiceController")
    http_server_app_ssl = SSLify(http_server_app)
    http_server_app.secret_key = secrets.token_hex()

    def __init__(self):
        self.repository = Repository()
        self.service = UserService(self.repository)

        self.http_server_app.logger.setLevel(logging.INFO)
        self.setup_routes()

    def setup_routes(self):
        self.http_server_app.add_url_rule(
            "/api/v1/register", methods=["PUT"], view_func=self.service.register_user
        )
        self.http_server_app.add_url_rule(
            "/api/v1/createSession",
            methods=["POST"],
            view_func=self.service.register_user,
        )
        self.http_server_app.add_url_rule(
            "/api/v1/addHouse", methods=["PUT"], view_func=self.service.add_house
        )
        self.http_server_app.add_url_rule(
            "/api/v1/updateUser", methods=["POST"], view_func=self.service.update_user
        )
        self.http_server_app.add_url_rule(
            "/api/v1/userInfo",
            methods=["GET"],
            view_func=self.service.get_user,
        )
        self.http_server_app.add_url_rule(
            "/api/v1/removeHouse",
            methods=["DELETE"],
            view_func=self.service.remove_house,
        )

    def run(self, host: str, port: int, ssl_context: Union[str, None] = None):
        self.http_server_app.run(host=host, port=port, ssl_context=ssl_context)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        prog="server_test.py",
        description="""
        Test server for UserServiceController. Request examples:
            curl --insecure -X GET  https://127.0.0.1:8083/temperature
        """,
        epilog="Example: python3 server.py -p 8083",
    )
    parser.add_argument(
        "-p",
        "--port",
        type=int,
        required=False,
        default=8083,
        help="Port for HTTPS server",
    )
    args = parser.parse_args()

    UserServiceController().run("0.0.0.0", args.port, ssl_context="adhoc")
