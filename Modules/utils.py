from flask import jsonify

from itertools import zip_longest

def merge_lists_remove_duplicates(lists):
    seen = set()  # 중복 체크용
    result = []

    # zip_longest를 사용하여 여러 리스트를 병렬로 순회
    for items in zip_longest(*lists, fillvalue=None):
        for item in items:
            if item is not None and item not in seen:  # 중복 제거
                seen.add(item)
                result.append(item)

    return result

import typing
from typing import get_origin, get_args

def is_valid_type(value, expected_type):
    origin = get_origin(expected_type)
    args = get_args(expected_type)

    if origin is list:
        if not isinstance(value, list):
            return False
        if args:  # e.g. list[str]
            return all(isinstance(item, args[0]) for item in value)
        return True

    elif origin is dict:
        if not isinstance(value, dict):
            return False
        if args:  # e.g. dict[str, int]
            key_type, value_type = args
            return all(isinstance(k, key_type) and isinstance(v, value_type)
                       for k, v in value.items())
        return True

    elif origin is tuple:
        if not isinstance(value, tuple):
            return False
        if args:
            return all(isinstance(v, t) for v, t in zip(value, args))
        return True

    elif isinstance(expected_type, type):
        return isinstance(value, expected_type)

    return False

def confirm_request(data,
                    required: dict[str, typing.Union[typing.Type, tuple[typing.Type, list]]]):
    if not data:
        return jsonify({"error": "Please provide JSON request body."}), 400
    for key, value in required.items():
        if type(value) is tuple and len(value) == 2:
            value_type = value[0]
            available_value = value[1]
        else:
            value_type = value
            available_value = None
        if key not in data:
            return jsonify({"error": f"Please provide '{key}' field in the JSON request body."}), 400
        if not is_valid_type(data[key], value_type):
            return jsonify({"error": f"Field '{key}' has invalid datatype. Expected {value_type}, got {type(data[key])}"}), 400
        if available_value and data[key] not in available_value:
            return jsonify({"error": f"Field '{key}' has invalid value. "
                                     f"Expected in {available_value}, got {data[key]}"}), 400
    return None

import uuid
import typing
def generate_integer_id64(existing_ids:typing.Iterable[int]=None):
    """64비트 무작위 정수 ID를 생성해서 반환하는 함수."""
    # uuid4()를 사용하여 무작위 UUID 생성 후, 정수형으로 변환
    if not existing_ids:
        return uuid.uuid4().int % (1 << 63)
    while (new_id:=uuid.uuid4().int % (1 << 63)) in existing_ids:
        pass
    return new_id


def compare_dicts_sorted(
        dict1: dict[str, list[int]],
        dict2: dict[str, list[int]]
) -> bool:
    """두 개의 딕셔너리를 비교해서 원소가 완벽히 동일하면 참을, 그렇지 않으면 거짓을 반환하는 함수."""
    # 두 딕셔너리의 키 집합이 동일한지 확인
    if set(dict1.keys()) != set(dict2.keys()):
        return False

    # 각 키별로 정렬된 리스트를 비교 (중복을 고려)
    for key in dict1:
        if sorted(dict1[key]) != sorted(dict2[key]):
            return False

    return True


import requests

def request_api(**request_kwargs):
    """간단한 API 요청 함수."""
    if request_kwargs.get('method') == "GET":
        return requests.get(request_kwargs.get('url'), params=request_kwargs['params'])
    elif request_kwargs.get('method') == "POST":
        return requests.post(request_kwargs.get('url'), json=request_kwargs['data'])
    else:
        return requests.models.Response()


def api_test(**request_kwargs):
    response = request_api(**request_kwargs)
    try:
        result = response.json()
    except Exception as e:
        result = response.reason

    print(f"status: {response.status_code}")
    print(f"response:")
    if isinstance(result, dict):
        for key, value in result.items():
            print(f"\t{key}: {value}")
    else:
        print(result)