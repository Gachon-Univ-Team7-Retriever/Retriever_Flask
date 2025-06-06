from pymongo import MongoClient
import torch
import numpy as np
import re
from urllib.parse import urlparse
from sklearn.cluster import DBSCAN
from sklearn.metrics.pairwise import cosine_similarity
from transformers import AutoTokenizer, AutoModel
from bs4 import BeautifulSoup
from tqdm import tqdm

from server.db import Database
from server.logger import logger

# MongoDB 연결
collection = Database.Collection.POST
cluster_collection = Database.Collection.POST_CLUSTERS

# KoBERT 모델 불러오기
model_name = "monologg/kobert"
tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
model = AutoModel.from_pretrained(model_name, trust_remote_code=True)

# HTML 정제 함수
def preprocess_html(html_content):
    soup = BeautifulSoup(html_content, 'html.parser')
    return soup.get_text(separator=' ').strip()

# KoBERT 임베딩 추출
def get_bert_embedding(text):
    tokens = tokenizer(text, return_tensors="pt", truncation=True, max_length=512, padding="max_length")
    with torch.no_grad():
        output = model(**tokens)
    return output.last_hidden_state[:, 0, :].squeeze().tolist()

# 도메인 추출
def extract_domain(url):
    try:
        return urlparse(url).netloc
    except:
        return None

# 텔레그램 채널 ID 추출
def extract_channel_id(url):
    match = re.search(r'(t\.me|telegram\.me)/([a-zA-Z0-9_]+)', url)
    if match:
        return match.group(2)
    return None

# 클러스터링 수행
def dbscan_clustering(embeddings, eps=0.4, min_samples=2):
    similarity_matrix = cosine_similarity(np.array(embeddings))
    similarity_matrix = (similarity_matrix + 1) / 2  # 정규화: [-1, 1] → [0, 1]

    distance_matrix = 1 - similarity_matrix
    distance_matrix = np.clip(distance_matrix, 0, 1)  # 음수 방지

    db = DBSCAN(metric="precomputed", eps=eps, min_samples=min_samples)
    return db.fit_predict(distance_matrix)

# 전체 클러스터링 프로세스
def perform_clustering_with_cosine(eps=0.4, min_samples=2):
    # 데이터 불러오기
    documents = list(collection.find({}, {
        "_id": 1, "postId": 1, "html": 1, "createdAt": 1, "updatedAt": 1, "promoSiteLink": 1
    }))

    if not documents:
        return {"error": "No documents found."}

    # 임베딩 생성
    embeddings = []
    for doc in tqdm(documents, desc="Generating embeddings"):
        text = preprocess_html(doc.get("html", ""))
        embeddings.append(get_bert_embedding(text))

    # 클러스터링
    labels = dbscan_clustering(embeddings, eps, min_samples)

    # 이전 클러스터 데이터 삭제
    cluster_collection.delete_many({})

    # 클러스터 저장
    for idx, doc in enumerate(documents):
        promo_link = doc.get("promoSiteLink")
        cluster_data = {
            "postId": doc.get("postId"),
            "cluster_label": int(labels[idx]),
            "embedding": embeddings[idx],
            "promoSiteLink": promo_link,
            "promoSiteName": extract_domain(promo_link) if promo_link else None,
            "channelId": extract_channel_id(promo_link) if promo_link else None,
            "createdAt": doc.get("createdAt"),
            "updatedAt": doc.get("updatedAt")
        }
        cluster_collection.insert_one(cluster_data)

    # 통계 저장
    cluster_collection.insert_one({
        "_id": "cluster_stats",
        "total_documents": len(documents),
        "noise_documents": list(labels).count(-1)
    })

    return {"message": "Clustering + promo info 저장 완료!"}
