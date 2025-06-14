"""텔레그램 메시지/엔티티 정보 추출 및 미디어 다운로드 유틸리티 모듈."""
from telethon.sync import types
from server.logger import logger
from typing import Optional

def extract_sender_info(sender):
    """텔레그램 메시지의 발신자 정보를 딕셔너리로 추출합니다.

    Args:
        sender: Telethon User/Channel 객체
    Returns:
        dict: 발신자 정보
    """
    if sender and isinstance(sender, types.User):
        return {
            "type": "user",
            "name": sender.username or None,
            "firstName": sender.first_name or None,
            "lastName": sender.last_name or None,
            "senderId": sender.id,
        }
    elif sender and isinstance(sender, types.Channel):
        return {
            "type": "channel",
            "name": sender.title or None,
            "senderId": sender.id,
        }
    return {
        "type": "unknown",
        "name": None,
        "senderId": None
    }


def get_message_url_from_event(event):
    """이벤트 객체에서 메시지 URL을 생성합니다.

    Args:
        event: Telethon 이벤트 객체
    Returns:
        str|None: 메시지 URL 또는 None
    """
    if event.chat:
        message_id = event.message.id  # 메시지 ID

        if event.chat.username:  # 공개 채널
            channel_username = event.chat.username
            return f"https://t.me/{channel_username}/{message_id}"
        else:  # 비공개 채널
            channel_id = abs(event.chat.id)  # 절댓값 사용
            return f"https://t.me/c/{channel_id}/{message_id}"

    return None  # 채널 정보 없음


def get_url_from_message(entity, message):
    """엔티티와 메시지 객체로부터 메시지 URL을 생성합니다.

    Args:
        entity: Telethon 채널/유저 객체
        message: Telethon 메시지 객체
    Returns:
        str|None: 메시지 URL 또는 None
    """
    if message.id:
        if entity.username: # 공개(public) 채널
            return f"https://t.me/{entity.username}/{message.id}"
        else: # 비공개(private) 채널
            return f"https://t.me/c/{str(entity.id).replace('-100', '')}/{message.id}"

    return None  # 채널 정보 없음


from telethon.tl.types import MessageMediaDocument, MessageMediaPhoto
async def download_media(message, client) -> (Optional[bytes], Optional[str]):
    """텔레그램 메세지에 미디어가 포함되어 있을 경우 미디어 데이터를 바이트 객체로 반환하고, 없으면 (None, None)을 반환하는 함수.

    Args:
        message: Telethon 메시지 객체
        client: Telethon 클라이언트
    Returns:
        tuple: (미디어 바이트, MIME 타입) 또는 (None, None)
    """
    if message.media and isinstance(message.media, (MessageMediaPhoto, MessageMediaDocument)):
        try:
            media_bytes = await client.download_media(
                message=message,
                file=bytes
            )
            # 미디어 타입 확인
            if isinstance(message.media, MessageMediaDocument) and hasattr(message.media, "document"):
                media_type = message.media.document.mime_type # 문서의 MIME 타입 사용 (비디오 포함)
            elif isinstance(message.media, MessageMediaPhoto):
                media_type = "image/jpeg"  # 사진은 MIME 타입이 없고, 기본적으로 JPEG
            else:
                media_type = None

            return media_bytes, media_type
        except Exception as e:
            logger.error(f"Failed to download media: {e}")
            return None, None
    return None, None
