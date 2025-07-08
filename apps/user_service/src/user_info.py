#!/usr/bin/env python3
# -*- coding: utf-8 -*-


class UserInfo:
    email: str
    password: str


class UserInfoDetailed(UserInfo):
    first_name: str
    second_name: str
    avatar_base64: str
    timezone: int
