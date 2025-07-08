from user_info import UserInfo, UserInfoDetailed


class Repository:

    def get_user_info(self, token: str):
        return UserInfo()

    def set_user_info(self, new_info: UserInfo):
        return True

    def get_user_detailed_info(self, token: str):
        return UserInfoDetailed()

    def set_user_detailed_info(self, new_info: UserInfoDetailed):
        return True
