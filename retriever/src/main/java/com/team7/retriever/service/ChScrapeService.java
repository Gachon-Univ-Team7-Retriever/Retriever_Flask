package com.team7.retriever.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.retriever.dto.ChDataResponse;
import com.team7.retriever.entity.ChData;
import com.team7.retriever.entity.ChInfo;
import com.team7.retriever.repository.ChDataRepository;
import com.team7.retriever.repository.ChInfoRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ChScrapeService {
    private final RestTemplate restTemplate;
    private final ChDataRepository chDataRepository;
    private final ChInfoRepository chInfoRepository;

    public ChScrapeService(RestTemplate restTemplate, ChDataRepository chDataRepository, ChInfoRepository chInfoRepository) {
        this.restTemplate = restTemplate;
        this.chDataRepository = chDataRepository;
        this.chInfoRepository = chInfoRepository;
    }

    /*
    백엔드 측 수정 사항 비교

    private String id; O
    private String name; -> TITLE
    -> PRIVATE STRING USERNAME
    -> PRIVATE BOOLEAN RESTRICTED
    private String link; -> X
    private String description; -> O 보류 BE
    -> PRIVATE LOCALDATETIME STARTED AT
    private LocalDateTime createdAt; -> DISCOVERED AT
    private LocalDateTime updatedAt; -> O
    private LocalDateTime deletedAt; -> O 보류
    -> PRIVATE LOCALDATETIME CHATBOT UPDATED AT
    private int promoCount; -> NEW BE
    private String status; -> NEW BE
     */

    // 스케줄 1 에서 실행되는 메서드
    public void channelScrape(String channelKey) {
        String infoAPI = "http://127.0.0.1:5000/telegram/channel/info";
        String api = "http://127.0.0.1:5000/telegram/channel/scrape";

        Map<String, String> requestBody = Map.of("channel_key", channelKey);
        ResponseEntity<String> infoResponse = restTemplate.postForEntity(infoAPI, requestBody, String.class); // 사용X
        ChDataResponse response = restTemplate.postForObject(api, requestBody, ChDataResponse.class);
        String message = response.getMessage();
        String status = response.getStatus();
        System.out.println("\t\t\t\t[ChScrapeService] " + message);
        System.out.println("\t\t\t\t[ChScrapeService] 채널 " + channelKey + " 스크랩 상태: " +  status);

        // saveChDataList(responseBody, channelKey); // -> 기존 직접 저장하는 메서드 호출하던 부분 -> 삭제
    }

    /*
    public String postScrapeData(String requestBody, String channelName) {
        // Flask API URL
        String api = "http://127.0.0.1:5000/telegram/channel/scrape";

        // HTTP Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // RequestBody -> HttpEntity로 감싸기
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Flask API에서 response 받기
            ResponseEntity<String> response = restTemplate.exchange(api, HttpMethod.POST, entity, String.class);

            // responseBody 반환
            String responseBody = response.getBody();

            // response를 ChData 객체로 변환하고 저장
            saveChDataList(responseBody, channelName);

            // 저장된 데이터를 반환
            return responseBody;
        } catch (HttpClientErrorException e) {
            // 예외 처리 및 오류 메시지 반환
            System.out.println("Error: " + e.getStatusCode());
            System.out.println("Error: " + e.getResponseBodyAsString());
            return "Error: " + e.getResponseBodyAsString();
        }
    }

     */

    /*
    private void saveChDataList(String responseBody, String channelName) {
        try {
            // JSON 형식 응답을 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            LocalDateTime currentDateTime = LocalDateTime.now();

            ChInfo chInfo = new ChInfo();
            chInfo.setId(channelName);
            chInfo.setName(channelName);
            chInfo.setLink("t.me/"+channelName);
            chInfo.setDescription(null);
            chInfo.setCreatedAt(currentDateTime);
            chInfo.setUpdatedAt(currentDateTime);
            chInfo.setDeletedAt(null);
            chInfoRepository.save(chInfo);
            System.out.println("ChInfo Saved");

            // 응답은 배열 형태
            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    // 각 항목에 대해 ChData 객체 생성
                    ChData chData = new ChData();

                    // channelId 생성 (channelName)
                    chData.setChannelId(channelName);

                    // ***
                    // 날짜 형식 파싱 (yyyy-MM-dd HH:mm:ss 형식 처리)
                    String dateStr = node.hasNonNull("date") ? node.get("date").asText() : null;
                    if (dateStr != null) {
                        // 포맷: "yyyy-MM-dd HH:mm:ss"
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        chData.setTimestamp(LocalDateTime.parse(dateStr, formatter));
                    }

                    // 필드 값이 null이면 그대로 null 저장
                    chData.setFirstName(node.hasNonNull("first_name") ? node.get("first_name").asText() : null);
                    chData.setLastName(node.hasNonNull("last_name") ? node.get("last_name").asText() : null);
                    chData.setImage(node.hasNonNull("media") ? node.get("media").asText() : null); // Base64 인코딩된 이미지 값
                    chData.setText(node.hasNonNull("text") ? node.get("text").asText() : null);
                    chData.setMsgUrl(node.hasNonNull("url") ? node.get("url").asText() : null);
                    chData.setUserId(node.hasNonNull("user_id") ? node.get("user_id").asText() : null);
                    chData.setUserName(node.hasNonNull("username") ? node.get("username").asText() : null);

                    // ChData 저장
                    chDataRepository.save(chData);
                    System.out.println("ChData Saved");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     */

}
