package com.team7.retriever.repository;

import com.team7.retriever.entity.PostHtml;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostHtmlRepository extends MongoRepository<PostHtml, String> {
    boolean existsByUrl(String url);
}
