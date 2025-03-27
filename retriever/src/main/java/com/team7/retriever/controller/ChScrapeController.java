package com.team7.retriever.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.retriever.service.ChScrapeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChScrapeController {
    private final ChScrapeService chScrapeService;

    public ChScrapeController(ChScrapeService chScrapeService) {
        this.chScrapeService = chScrapeService;
    }

    // 정상 동작하는 기본 컨트롤러
    /*
    @PostMapping("/telegram/channel/scrape")
    public String channelScrape(@RequestBody String requestBody) {
        return chScrapeService.postScrapeData(requestBody);
    }
     */

    // 직접 실행하는 코드 (업데이트X)
    /*
    @PostMapping("/telegram/channel/scrape")
    public String channelScrape(@RequestBody String requestBody) {
        try {
            // JSON 파싱을 통해 "channel_name" 값을 추출
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(requestBody);

            // "channel_name" 필드를 추출
            String channelName = jsonNode.hasNonNull("channel_name") ? jsonNode.get("channel_name").asText() : null;
            System.out.println(channelName);

            // channelName이 null이 아니면 서비스로 전달
            if (channelName != null) {
                return chScrapeService.postScrapeData(requestBody, channelName);
            } else {
                return "Error: channel_name is missing";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Invalid JSON format";
        }
    }

     */


}
