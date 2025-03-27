package com.team7.retriever.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "chat_bots")
public class ChatBots {
    @Id
    private String id;
    private long channelId;
    private LocalDateTime updatedAt;
    private List<Integer> chatIds = new ArrayList<>();
}
