package com.team7.retriever.service;

import com.team7.retriever.entity.ChData;
import com.team7.retriever.repository.ChDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChDataService {
    @Autowired
    private ChDataRepository chDataRepository;

    @Autowired
    public ChDataService(ChDataRepository chDataRepository) {
        this.chDataRepository = chDataRepository;
    }

    // 전체 조회
    public List<ChData> getAllChannelData() {
        return chDataRepository.findAll();
    }

    // 유저 아이디로 조회
    public List<ChData> getChannelDataBySenderId(long id) {
        return chDataRepository.findBySender_Id(id);
    }

    // 유저 이름으로 조회
    public List<ChData> getChannelDataBySederName(String senderName) {
        return chDataRepository.findBySender_Name(senderName);
    }

    // 채널 아이디로 조회
    public List<ChData> getChannelDataByChannelId(long channelId) {
        return chDataRepository.findByChannelId(channelId);
    }

    // 메시지 url로 조회
    public List<ChData> getChannelDataByUrl(String url) {
        return chDataRepository.findByUrl(url);
    }

    // 메시지 내용으로 조회 (포함되는 것)
    public List<ChData> getChannelDataByText(String text) {
        return chDataRepository.findByText(text);
    }
}
