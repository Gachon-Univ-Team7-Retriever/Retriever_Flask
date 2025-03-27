package com.team7.retriever.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.retriever.entity.PostHtml;
import com.team7.retriever.repository.PostHtmlRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class HtmlCrawlingService {

    private final RestTemplate restTemplate;
    private final PostHtmlRepository postHtmlRepository;
    private final ObjectMapper objectMapper;

    public HtmlCrawlingService(RestTemplate restTemplate, PostHtmlRepository postHtmlRepository, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.postHtmlRepository = postHtmlRepository;
        this.objectMapper = objectMapper;
    }

    public String crawlHtml(String link) {
        String api = "http://127.0.0.1:5000/crawl/html";
        // ResponseEntity<String> response = restTemplate.postForEntity(api, link, String.class);
        Map<String, String> requestBody = Map.of("link", link);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(api, requestBody, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("\t[HtmlCrawlingService] HTML 크롤링 완료");
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String html = jsonNode.asText();
                // System.out.println("JsonNode TEST");
                // System.out.println(html);
                // System.out.println();
                System.out.println("\t[HtmlCrawlingService] HTML 디코딩 완료");
                
                return html; // 크롤링 결과 반환
            } else {
                throw new RuntimeException("\t[HtmlCrawlingService] HTML is null : " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("\t[HtmlCrawlingService] HTML 크롤링 중 오류 발생: " + e.getMessage(), e);
        }
        // return response.getBody();
    }

    public void saveHtml(String html, String url) {
        PostHtml postHtml;
        LocalDateTime now = LocalDateTime.now();
        // String id = "html_" + url; // 아이디 자동 생성

        postHtml = new PostHtml();
        postHtml.setHtml(html);
        postHtml.setUrl(url);
        postHtml.setCreatedAt(now);
        postHtml.setUpdatedAt(now);
        postHtmlRepository.save(postHtml);
        System.out.println("\t[HtmlCrawlingService] " + url + " saved"); // test code

        // DB에 존재하지 않아야 실행되도록 수정됨 -> 아래 코드 삭제
        /*
        Optional<PostHtml> existingEntry = postHtmlRepository.findById(id);
        if (!existingEntry.isPresent()) { // DB에 존재하지 않으면
            postHtml = new PostHtml();
            postHtml.setId(id);
            postHtml.setPostId("post_" + url);
            postHtml.setCreatedAt(now);
            postHtml.setUrl(url);
        } else  { // DB에 이미 존재하는 경우 (update)
            System.out.println("\t[HtmlCrawlingService] " + id + " is already exist"); // test code
            postHtml = existingEntry.get();
        }
        postHtml.setHtml(html); // 삭제된 게시물이 html 단계에서 걸러지는 거면 이거 if 안으로 넣어야 됨
        postHtml.setUpdatedAt(now);
        postHtmlRepository.save(postHtml);
        System.out.println("\t[HtmlCrawlingService] " + id + " saved"); // test code
        // DB 저장 완료

         */
    }
}
