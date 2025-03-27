package com.team7.retriever.service;


import com.team7.retriever.entity.ChInfo;
import com.team7.retriever.repository.ChInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChInfoService {
    @Autowired
    private final ChInfoRepository chInfoRepository;

    public ChInfoService(ChInfoRepository chInfoRepository) {
        this.chInfoRepository = chInfoRepository;
    }

    // 모든 채널 정보 조회
    public List<ChInfo> getAllChannelInfo() {
        return chInfoRepository.findAll();
    }

    // 채널 아이디로 해당 채널 조회
    public Optional<ChInfo> getChannelInfoById(String id) {
        return chInfoRepository.findById(id);
    }

    // 채널 링크로 해당 채널 조회
    public Optional<ChInfo> getChannelByLink(String link) {
        return chInfoRepository.findByLink(link);
    }

    /* 250102 추가 */
    // 채널 이름에 포함
    public List<ChInfo> getChannelInfoByNameContaining(String name) {
        return chInfoRepository.findByNameContaining(name);
    }

    public boolean isChannelExists(String id) {
        return chInfoRepository.existsById(id);
    }
}
