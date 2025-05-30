import typing

from telethon.sync import TelegramClient, types
from telethon.tl.functions.messages import CheckChatInviteRequest, ImportChatInviteRequest
from telethon.errors import InviteHashEmptyError, InviteHashExpiredError, InviteHashInvalidError, ChannelInvalidError, \
    ChannelPrivateError, FloodWaitError

from server.logger import logger

if typing.TYPE_CHECKING:
    from .manager import TelegramManager


class ConnectMethods:
    def __init__(self):
        super().__init__()

    async def accept_invitation(self:'TelegramManager', invite_link:str):
        invite_hash = invite_link.split("+")[1]
        entity = None

        try:
            # 초대 링크 유효성 검사 및 채널 정보 가져오기
            invite_info = await self.client(CheckChatInviteRequest(invite_hash))

            # 초대 링크가 유효하지만 아직 참여하지 않은 경우
            if isinstance(invite_info, types.ChatInvite):
                logger.debug(f"Joining the channel via invite link...")
                entity = await self.client(ImportChatInviteRequest(invite_hash))
                logger.debug(f"Successfully joined the channel via invite link.")
            # 이미 채널에 참여 중인 경우
            elif isinstance(invite_info, types.ChatInviteAlready):
                entity = await self.client.get_entity(invite_info.chat)
                logger.warning(f"Already a participant in the channel. Retrieved entity.")

        except InviteHashEmptyError as e:
            logger.error(f"The invite hash is empty. error message: {e}")
        except InviteHashExpiredError as e:
            logger.error(f"The chat the user tried to join has expired and is not valid anymore. error message: {e}")
        except InviteHashInvalidError as e:
            logger.error(f"The invite hash is invalid. error message: {e}")
        except Exception as e:
            logger.error(f"Failed to process invite link: {e}")

        return entity

    async def connect_channel(self:'TelegramManager', channel_key:typing.Union[int, str]):
        logger.debug(f"Connecting to the channel... Channel key: {channel_key}")
        try:
            # 초대 링크 처리 (비공개 채널일 경우 필수 작업)
            if isinstance(channel_key, str) and channel_key.startswith("+"):
                logger.debug(f"Type of connection key is invite link. processing invite link... (invite link: {channel_key})")
                entity = await self.accept_invitation(channel_key)
            else:
                if isinstance(channel_key, int):  # 정수 형태의 채널 ID 처리
                    logger.debug(f"Type of connection key is Channel ID. processing Channel ID... (Channel ID: {channel_key})")
                else:  # 문자열 형태의 채널 username 처리
                    logger.debug(f"Type of connection key is Channel @username. processing Channel @username... (@username: {channel_key})")
                entity = await self.client.get_entity(channel_key)

            if not entity:
                logger.warning(f"Failed to connect the channel. Failed to retrieve entity for the channel. Channel ID or @username: {channel_key}")

            return entity
        except (FloodWaitError,
                ChannelPrivateError,
                ChannelInvalidError,
                InviteHashEmptyError,
                InviteHashExpiredError,
                InviteHashInvalidError,
                ValueError,
                ) as e:
            if isinstance(e, FloodWaitError):
                wait_time = e.seconds
                logger.warning(f"요청이 너무 많아 제한되었습니다. {wait_time}초 후에 다시 시도하세요.")
            elif isinstance(e, ChannelPrivateError):
                logger.warning(f"The channel is private and you are not invited yet. Channel ID or @username: {channel_key}")
            elif isinstance(e, ChannelInvalidError):
                logger.warning(f"Invalid Channel ID or @username: {channel_key}")
            elif isinstance(e, ValueError):
                logger.warning(e)
            logger.warning("Failed to connect to the channel.")
            return None
        except Exception as e:
            logger.error(f"Error in connect_channel(): {e}")
            return None
