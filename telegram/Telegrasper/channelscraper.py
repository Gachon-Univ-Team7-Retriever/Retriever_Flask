"""텔레그램 채널 콘텐츠 스크래핑 및 처리 기능을 제공하는 모듈."""
import asyncio
import os
import typing

from ai.telegram import check_telegram_by_openai
from server.cypher import run_cypher, Neo4j
from server.db import Database
from server.google import *
from server.logger import logger
from .utils import download_media, extract_sender_info, get_url_from_message

default_bucket_name = os.environ.get('GCS_BUCKET_NAME')

if typing.TYPE_CHECKING:
    from .manager import TelegramManager

class ChannelContentMethods:
    """텔레그램 채널의 콘텐츠를 검사하고 수집하는 메서드를 제공하는 클래스입니다."""
    
    async def check_channel_content(self:'TelegramManager', channel) -> bool:
        """채널의 데이터를 일부 수집해서, 마약 관련 채널로 강력히 의심되는지 확인하는 검문 메서드.

        Args:
            channel: 채널 엔티티 객체 또는 채널 ID/username/초대 링크
        Returns:
            bool: 마약 관련 채널로 의심되는지 여부
        """
        try:
            if isinstance(channel, (int, str)): # channel key의 형태로 입력되었을 경우, 채널에 연결하고 entity 반환 필요
                entity = await self.connect_channel(channel)
                if entity is None:
                    logger.debug(f"텔레그램 채널 검문 결과: False, 사유: 채널에 연결 불가능. Channel ID, @username or invite link: {channel}")
                    return False
            else:
                entity = channel # channel entity 객체로 입력되었을 경우 그대로 사용

            # 메시지 확인
            merged_message:str = ""
            chat_num = 1
            async for post in self.client.iter_messages(entity):
                if chat_num > 10:
                    break # 최대 10개의 채팅만 수집
                if post.text:
                    merged_message += f"chat #{chat_num}: {post.text}\n"
                    chat_num += 1

            result = check_telegram_by_openai(merged_message)
            logger.debug(f"텔레그램 채널 검문 결과: {result}, 채널 ID: {entity.id}, 채널 Title: {entity.title}, 사유: OpenAI의 판정")
            return result

        except Exception as e:
            logger.error(f"An error occurred in check_channel_content(): {e}")
            logger.debug(f"텔레그램 채널 검문 결과: False, 사유: 오류 발생")
            return False

    async def scrape_channel_content(self:'TelegramManager', channel_key:typing.Union[int, str]) -> dict:
        """채널의 모든 메시지를 수집하고 저장하는 비동기 메서드.

        Args:
            channel_key (int|str): 채널 ID, @username, 또는 초대 링크
        Returns:
            dict: 수집 결과 상태와 메시지를 포함하는 딕셔너리
        """
        try:
            entity = await self.connect_channel(channel_key)
            if entity is None:
                return {"status": "warning",
                        "message": "Failed to connect to the channel."}
            # 메시지 스크랩
            post_count = 0

            # GCS 버킷에 채팅방 ID로 폴더 생성
            create_folder(default_bucket_name, entity.id)

            async for message in self.client.iter_messages(entity):
                if post_count > 500: # 채널 채팅 수 최대 500개 수집으로 제한.
                    break

                post_count += 1
                if post_count % 10 == 0:
                    logger.info(f"{post_count} Posts is scraped from {channel_key}")

                await process_message(entity, self.client, message)

        except Exception as e:
            msg = f"An error occurred in scrape_channel_content(): {e}"
            logger.error(msg)
            return {"status": "error",
                    "message": msg}

        else:
            msg = (f"Archived all chats for the channel(Channel key: {channel_key}) in MongoDB - "
                   f"DB: {Database.NAME}, collection: {Database.Collection.Channel.DATA}, channel ID: {entity.id}")
            logger.info(msg)
            return {"status": "success",
                    "message": msg}

    def scrape(self:'TelegramManager', channel_key: typing.Union[int, str]) -> dict:
        """채널 데이터 수집을 동기적으로 실행하는 래퍼 메서드.

        Args:
            channel_key (int|str): 채널 ID, @username, 또는 초대 링크
        Returns:
            dict: 수집 결과 상태와 메시지를 포함하는 딕셔너리
        """
        future = asyncio.run_coroutine_threadsafe(self.scrape_channel_content(channel_key), self.loop)
        return future.result()  # 블로킹 호출 (결과를 기다림)

    def check(self:'TelegramManager', channel_key: typing.Union[int, str]) -> bool:
        """채널 데이터 의심도 검증을 동기적으로 실행하는 래퍼 메서드.

        Args:
            channel_key (int|str): 채널 ID, @username, 또는 초대 링크
        Returns:
            bool: 마약 관련 채널로 의심되는지 여부
        """
        future = asyncio.run_coroutine_threadsafe(self.check_channel_content(channel_key), self.loop)
        return future.result()  # 블로킹 호출 (결과를 기다림)

async def process_message(entity, client, message) -> None:
    """텔레그램 메시지를 처리하고 저장하는 비동기 함수.

    Args:
        entity: 텔레그램 채널 엔티티
        client: 텔레그램 클라이언트
        message: 처리할 메시지 객체
    """
    chat_collection = Database.Collection.Channel.DATA  # 채팅 컬렉션 선택
    channel_collection = Database.Collection.Channel.INFO
    argot_collection = Database.Collection.ARGOT # 은어 컬렉션 선택
    drugs_collection = Database.Collection.DRUGS # 마약류 컬렉션 선택

    argot_list, drugs_list, argot_names = [], [], []
    # 은어가 메세지에서 발견될 경우 발견된 은어들과 그 은어에 대응하는 마약류를 리스트로 생성
    for argot in argot_collection.find():
        if message.text and (argot_name:=argot.get("name")) in message.text:
            argot_list.append(argot["_id"])
            argot_names.append(argot_name)
            drug = drugs_collection.find_one({"_id": argot.get("drugId")})
            drugs_list.append(drug.get("_id"))

            ##### Neo4j #####
            # 채널과 발견된 은어 간의 관계를 Neo4j 그래프 데이터베이스에 추가
            # 먼저 은어 노드가 데이터베이스에 없을 경우 추가
            summary = run_cypher(query=Neo4j.QueryTemplate.Node.Argot.MERGE,
                                 parameters={
                                     "name": argot_name,
                                     "drugId": argot.get("drugId"),
                                 }).consume()
            if summary.counters.nodes_created > 0:
                logger.info(f"새로운 마약 용어가 발견되어 Neo4j 데이터베이스에 추가되었습니다. 발견된 채널의 ID: {entity.id}, 은어: `{argot_name}`")
            # 마약류 노드가 데이터베이스에 없을 경우 추가
            summary = run_cypher(query=Neo4j.QueryTemplate.Node.Drug.MERGE,
                                 parameters={
                                     "id": drug.get("_id"),
                                     "name": drug.get("drugName"),
                                     "type": drug.get("drugType"),
                                     "englishName": drug.get("drugEnglishName"),
                                 }).consume()
            if summary.counters.nodes_created > 0:
                logger.info(f"새로운 마약류가 발견되어 Neo4j 데이터베이스에 추가되었습니다. 발견된 채널의 ID: {entity.id}, 마약류: `{drug.get("drugName")}`")

            # 판매 관계가 없을 경우 판매 관계를 생성하고, 현재 chatId를 관계의 속성에 추가
            run_cypher(query=Neo4j.QueryTemplate.Edge.SELLS,
                       parameters={
                           "channelId": entity.id,
                           "argotName": argot_name,
                           "chatId": message.id,
                       })
            # 은어 -> 마약 대응 관계가 없을 경우 관계를 생성
            run_cypher(query=Neo4j.QueryTemplate.Edge.REFERS_TO,
                       parameters={
                           "argotName": argot_name,
                           "drugId": drug.get("_id"),
                       })

    if argot_list:
        logger.debug(
            f"Argot and Drugs are found in chat(chat ID: {message.id}, channel ID: {entity.id}). Argot: {argot_names}, Drugs: {drugs_list}")
    
    ##### MongoDB #####
    # channelId 필드와 id 필드를 기준으로 이미 채팅이 수집되었는지 검사한 후, 아직 수집되지 않았을 경우에만 삽입
    if not chat_collection.find_one({"channelId": entity.id, "id": message.id}):
        # GCS 버킷에 이미 해당 채팅의 파일이 존재하는지 검사하고, 존재하지 않을 경우에만 미디어를 다운로드해서 버킷에 저장
        media_info = check_gcs_object_and_get_info(bucket_name=default_bucket_name,
                                                   folder_name=entity.id,
                                                   file_name=message.id)
        if not media_info:
            media_data, media_type = await download_media(message, client)
            if media_data:
                try:
                    # 미디어를 GCS 버킷에 업로드하고 업로드된 공개 URL을 받아온다.
                    media = {"url": upload_bytes_to_gcs(bucket_name=default_bucket_name,
                                                        folder_name=entity.id,
                                                        file_name=message.id,
                                                        file_bytes=media_data,
                                                        content_type=media_type),
                             "type": media_type}
                except Exception as e:
                    logger.error(f"An error occurred in process_message(), while uploading media to Google Cloud Storage: {e}")
                    media = None
                else:
                    logger.debug(f"Google Cloud Storage 버킷에 성공적으로 데이터를 저장했습니다. (Media type: {media_type})")
            else:
                # 미디어가 없을 경우 media는 None으로
                media = None
        else:
            logger.warning("GCS 버킷에 이미 해당 채팅의 미디어가 저장되어 있습니다. 미디어 저장을 건너뜁니다.")
            media = media_info

        post_data = {
            "channelId": entity.id,
            "timestamp": message.date,
            "text": message.text or "",
            "sender": extract_sender_info(message.sender),
            "views": message.views or None,
            "url": get_url_from_message(entity, message),
            "id": message.id,
            "media": media,
            "argot": argot_list,
            "drugs": drugs_list,
        }
        chat_collection.insert_one(post_data)

        # channel_info에서 마지막 채팅의 업데이트 시점 갱신
        channel_collection.update_one({"_id": entity.id},
                                      {"$set": {"updatedAt": message.date}},
                                      upsert=False)

        
    else: # 이미 수집된 채팅일 경우 경고만 출력
        logger.warning(
            f"MongoDB collection already has same unique index of a chat(channelId: {entity.id}, id: {message.id})")




