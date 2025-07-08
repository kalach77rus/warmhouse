#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import uuid

from flask import request
from functools import wraps, partial

from user_info import UserInfo, UserInfoDetailed
from relay_info import RelayInfo
from repository import Repository


class UserService:

    def __init__(self, repository: Repository):
        self.repository = repository
        self.known_tokens = set[str]()  # TODO: use persistant KV-storage

    @staticmethod
    def _auth_required(endpoint_method):
        @wraps(endpoint_method)
        def verify_token_wrap(self, *args, **kwargs):
            result = self.__verify_token()
            return (
                result if result is not None else endpoint_method(self, *args, **kwargs)
            )

        return verify_token_wrap

    def create_session(self, login: str, password: str):
        if not self._validate_credentials(login, password):
            return json.dumps({"error": "invalid creds"}), 401

        token = str(uuid.uuid4())
        self.known_tokens.add(token)
        return token

    def register_user(self, new_info: UserInfo):
        self.repository.set_user_info(new_info)
        return json.dumps({"result": "OK"}), 200

    @_auth_required
    def update_user(self, new_info: UserInfoDetailed):
        self.repository.set_user_detailed_info(new_info)
        return json.dumps({"result": "OK"}), 200

    @_auth_required
    def get_user(self):
        return json.dumps({"result": self.repository.get_user_detailed_info("")}), 200

    @_auth_required
    def add_house(self, new_info: UserInfoDetailed):
        return json.dumps({"result": "OK"}), 200

    @_auth_required
    def remove_house(self, new_info: UserInfoDetailed):
        return json.dumps({"result": "OK"}), 200

    @_auth_required
    def validate_token(self, token: str):
        return json.dumps({"result": (token in self.known_tokens)}), 200

    @_auth_required
    def validate_relay_info(self, relay: RelayInfo) -> bool:
        address = f"{relay.host}:{str(relay.port)}"
        return address != ""  # TODO: ping relay address

    def _validate_credentials(self, login: str, password: str) -> bool:
        return True  # TODO: call db to validate creds

    def __verify_token(self):
        token = request.headers.get("Authorization", None, type=str)
        if token is None:
            return json.dumps({"error": "No token"}), 401

        if token.startswith("Bearer "):
            token = token[7:]
        else:
            self._http_server_app.logger.warning("Token '%s' is not Bearer.", token)
            return json.dumps({"error": "Wrong token format"}), 401

        # 401 - Unauthorized
        if token not in self.registered_tokens:
            self._http_server_app.logger.warning(
                "Token '%s', is not registered, refusing.", token
            )
            return json.dumps({"error": "Wrong token"}), 401
        return None
