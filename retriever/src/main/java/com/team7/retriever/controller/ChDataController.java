package com.team7.retriever.controller;

import com.team7.retriever.entity.ChData;
import com.team7.retriever.service.ChDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChDataController {
    @Autowired
    private ChDataService dataService;

    // 전체 채팅 조회
    @GetMapping("/all")
    public List<ChData> getAllChannelData() {
        return dataService.getAllChannelData();
    }

    // 채널 아이디로 채팅 조회
    @GetMapping("/channel/{channelID}")
    public List<ChData> getChannelDataByChannelId(@PathVariable long channelID) {
        return dataService.getChannelDataByChannelId(channelID);
    }

    // 유저 아이디로 채팅 조회
    @GetMapping("/user/id/{senderID}") // 신규
    public List<ChData> getChannelDataBySenderId(@PathVariable long senderID) {
        return dataService.getChannelDataBySenderId(senderID);
    }

    @GetMapping("/user/name/{senderName}") // 신규
    public List<ChData> getChannelDataBySenderName(@PathVariable String senderName) {
        return dataService.getChannelDataBySederName(senderName);
    }

    // 메시지 url로 채팅 조회 (채팅 각각에 다 있음)
    @GetMapping("/by-msg-url/{msgUrl}")
    public List<ChData> getChannelDataByMsgUrl(@PathVariable String msgUrl) {
        return dataService.getChannelDataByUrl(msgUrl);
    }

    @GetMapping("/text") // 신규
    public List<ChData> getChannelDataByText(@RequestParam String text) {
        return dataService.getChannelDataByText(text);
    }
}
