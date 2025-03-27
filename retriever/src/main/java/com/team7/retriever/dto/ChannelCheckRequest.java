package com.team7.retriever.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChannelCheckRequest {
    @JsonProperty("channel_name")
    private String channel;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
