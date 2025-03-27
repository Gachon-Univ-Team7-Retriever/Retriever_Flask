package com.team7.retriever.service;

import com.team7.retriever.dto.ChannelCheckResponse;
import com.team7.retriever.entity.ChInfo;
import com.team7.retriever.repository.ChInfoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


@Service
public class ChannelCheckService {
    private final RestTemplate restTemplate;
    private final ChScrapeService chScrapeService;
    private final ChInfoRepository chInfoRepository;

    public ChannelCheckService(RestTemplate restTemplate, ChScrapeService chScrapeService, ChInfoRepository chInfoRepository) {
        this.restTemplate = restTemplate;
        this.chScrapeService = chScrapeService;
        this.chInfoRepository = chInfoRepository;
    }

    public ChannelCheckResponse checkChannel(String channel) {
        String api = "http://127.0.0.1:5000/telegram/channel/check-suspicious";
        Map<String, String> requestBody = Map.of("channel_name", channel);
        // ResponseEntity<Boolean> response = restTemplate.postForEntity(api, requestBody, Boolean.class);
        ResponseEntity<ChannelCheckResponse> response = restTemplate.postForEntity(api, requestBody, ChannelCheckResponse.class);
        // Boolean suspicious;
        // suspicious = (Boolean) response.getBody().get("suspicious");
        String suspicious = response.getBody().getSuspicious();
        System.out.println("\t\t\t[ChannelCheckService] 채널 검문 결과 (" + channel + ") " + suspicious);
        LocalDateTime now = LocalDateTime.now();
        ChInfo chInfo;
        // Optional<ChInfo> existingEntry = chInfoRepository.findById(channel);

        // 기존 chInfo 저장하는 부분
        // => 모듈에서 저장하는 것으로 변경되었음
        // => 모듈 저장값이 다가 아니라서,
        // 모듈에서 먼저 저장하고 가져오든지, (아이디 문제 때문에)
        // 모듈에서 전체 데이터 저장해주든지 하는 방식으로 수정해야 할 것 같음

        chInfo = new ChInfo();
        /*
        chInfo.setId(channel);
        chInfo.setName(channel);
        chInfo.setLink("t.me/"+channel);
        chInfo.setCreatedAt(now);
        chInfo.setUpdatedAt(now);

         */

        if (suspicious != null) {
            if (suspicious.equals("true")) {
                chInfo.setDescription("현재 활성 채널입니다.");
                chInfo.setStatus("ACTIVE");
                chInfoRepository.save(chInfo);
                System.out.println("\t\t\t[ChannelCheckService] ChInfo @" + channel + " saved");
                System.out.println("\t\t\t[ChannelCheckService] 채널 스크랩 시작 (" + channel + ")");
                // chScrapeService.channelScrape(channel); // 채널 스크랩 모듈 실행 // 소요 시간 때문에 주석 처리, 풀어줘야 함 !
                System.out.println("\t\t\t[ChannelCheckService] 채널 스크랩 실행 완료 (" + channel + ")"); // 처리 상태 X / 실행 완료 확인용
            } else {
                chInfo.setDescription("현재 비활성 채널입니다.");
                chInfo.setStatus("INACTIVE");
                chInfoRepository.save(chInfo);
                System.out.println("\t\t\t[ChannelCheckService] ChInfo @" + channel + " saved");
            }
        }

        return response.getBody();
    }
}