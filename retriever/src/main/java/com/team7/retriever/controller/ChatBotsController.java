package com.team7.retriever.controller;

import com.team7.retriever.entity.ChatBots;
import com.team7.retriever.service.ChatBotsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chatbots")
public class ChatBotsController {

    @Autowired
    private ChatBotsService chatBotsService;

    // 전체 조회
    @GetMapping("/all")
    public List<ChatBots> getAllChatBots() {
        return chatBotsService.getAllChatBots();
    }

    // 아이디로 조회
    @GetMapping("/id/{id}")
    public Optional<ChatBots> getChatBotsById(@PathVariable String id) {
        return chatBotsService.getChatBotsById(id);
    }

    // 채널 아이디로 조회
    @GetMapping("/chId/{channelId}")
    public List<ChatBots> getChatBotsByChannelId(@PathVariable int channelId) {
        return chatBotsService.getChatBotsByChannelId(channelId);
    }
}
