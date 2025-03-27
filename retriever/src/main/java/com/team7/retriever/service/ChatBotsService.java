package com.team7.retriever.service;

import com.team7.retriever.entity.ChatBots;
import com.team7.retriever.repository.ChatBotsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatBotsService {

    @Autowired
    private ChatBotsRepository chatBotsRepository;

    // all
    public List<ChatBots> getAllChatBots() {
        return chatBotsRepository.findAll();
    }

    // id
    public Optional<ChatBots> getChatBotsById(String id) {
        return chatBotsRepository.findById(id);
    }

    // channel id
    public List<ChatBots> getChatBotsByChannelId(int channelId) {
        return chatBotsRepository.findByChannelId(channelId);
    }
}
