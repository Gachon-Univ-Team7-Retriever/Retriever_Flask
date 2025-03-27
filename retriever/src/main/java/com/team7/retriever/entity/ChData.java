package com.team7.retriever.entity;

import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Document(collection = "channel_data")
public class ChData {

    // _id 자동 생성
    @Id
    @Field("_id")
    private String _id;
    private long channelId;
    private LocalDateTime timestamp;
    private String text;
    private senderInfo sender;
    private int views;
    private String url;
    @Field("id")
    private int id;
    private media media;

    @Getter
    public static class senderInfo {
        private String type;
        private String name;
        @Field("id")
        private long id;
    }

    @Getter
    public static class media {
        private String url;
        private String type;
    }
}
