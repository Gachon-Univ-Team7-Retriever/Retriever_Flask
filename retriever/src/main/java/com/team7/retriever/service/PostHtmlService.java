package com.team7.retriever.service;

import com.team7.retriever.repository.PostHtmlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostHtmlService {
    @Autowired
    private PostHtmlRepository postHtmlRepository;

    public boolean isUrlExists(String url) {
        return postHtmlRepository.existsByUrl(url);
    }
}
