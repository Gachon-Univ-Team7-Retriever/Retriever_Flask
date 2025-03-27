package com.team7.retriever.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.team7.retriever.entity.ChInfo;
import com.team7.retriever.entity.Posts;
import com.team7.retriever.repository.ChInfoRepository;
import com.team7.retriever.repository.PostsRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PreprocessService {
    private final RestTemplate restTemplate;
    private final PostsRepository postsRepository;
    private final PostsService postsService;
    private final ObjectMapper objectMapper;
    private final ChannelCheckService channelCheckService;
    private final HtmlCrawlingService htmlCrawlingService;
    private final ChInfoService chInfoService;
    private final ChInfoRepository chInfoRepository;

    public PreprocessService(RestTemplate restTemplate, PostsRepository postsRepository, PostsService postsService, ObjectMapper objectMapper, ChannelCheckService channelCheckService, HtmlCrawlingService htmlCrawlingService, ChInfoService chInfoService, ChInfoRepository chInfoRepository) {
        this.restTemplate = restTemplate;
        this.postsRepository = postsRepository;
        this.postsService = postsService;
        this.objectMapper = objectMapper;
        this.channelCheckService = channelCheckService;
        this.htmlCrawlingService = htmlCrawlingService;
        this.chInfoService = chInfoService;
        this.chInfoRepository = chInfoRepository;
    }

    // 스케줄 2 - 데이터 업데이트
    public String updatePreprocess(String html, String link) {
        String api = "http://127.0.0.1:5000/preprocess/extract/web-promotion";

        Map<String, String> requestBody = Map.of("html", html);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(api, requestBody, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) { // 다시 확인 필요
                throw new RuntimeException("\t\t[PreprocessService] HTML 전처리 실패: \" + response.getStatusCode()");
            }
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String content = jsonNode.get("promotion_content").asText(null); // null 기본값 사용 (promotion_content가 없거나 null인 경우)
            List<String> telegrams = objectMapper.convertValue(
                    jsonNode.get("telegrams"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

            System.out.println("\t\t[PreprocessService] 전처리 결과: " + content);
            if (content == "null" || content == null) {
                System.out.println("\t\t[PreprocessService] 마약 관련 홍보글이 아닙니다!");
                // 해당 링크 모든 데이터 삭제됨 상태로 설정
                setDeleted(link);
                System.out.println("\t\t[PreprocessService] 홍보글 삭제 처리 완료");
            } else { // 마약 관련 홍보글인 경우
                System.out.println("\t\t[PreprocessService] 마약 관련 홍보글입니다!");
                // 해당 링크 모든 데이터 updatedAt 업데이트
                // 해당 링크로 새로운 데이터 DB에 저장
                updateData(html, link, content, telegrams);
                System.out.println("\t\t[PreprocessService] 데이터 업데이트 완료");
            }
            return content;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("\t\t[PreprocessService] JSON 파싱 중 오류 발생: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("\t\t[PreprocessService] HTML 전처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // 데이터 삭제됨 상태로 변경
    private void setDeleted(String link) {
        List<Posts> posts = postsService.getPostsByLink(link);
        for (Posts post : posts) {
            post.setDeleted(true);
            post.setDeletedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            postsRepository.save(post);
        }
        System.out.println("\t\t[PreprocessService] " + posts.size() + "개의 데이터를 업데이트, 삭제 상태로 변경 완료");
    }

    // 데이터 업데이트
    // 1. 업데이트 시각 변경
    // 2. 업데이트 사항 있으면 새 데이터 저장 (데이터 저장 메서드 호출)
    private void updateData(String html, String link, String content, List<String> telegrams) {
        List<Posts> posts = postsService.getPostsByLink(link);
        for (Posts post : posts) {
            post.setUpdatedAt(LocalDateTime.now());
            postsRepository.save(post);
        }
        System.out.println("\t\t[PreprocessService] " + posts.size() + "개의 데이터 업데이트 시각 변경 완료");
        if (!posts.isEmpty()) {
            if (!posts.get(posts.size() - 1).getContent().equals(content)) { // 본문 내용이 동일하지 않으면 (테스트 완료)
                System.out.println("\t\t[PreprocessService] 홍보글에 업데이트 사항이 있습니다");
                saveData(html, link, content, telegrams);
                System.out.println("\t\t[PreprocessService] 최신 데이터 저장 완료");
            }
        }

    }

    // 데이터 새로 저장
    private void saveData(String html, String link, String content, List<String> telegrams) {
        htmlCrawlingService.saveHtml(html, link);

        Posts post = new Posts();
        post.setLink(link);
        post.setTag(null);
        post.setSiteName(null);
        post.setTitle(null);
        post.setContent(content);
        post.setPromoSiteLink(Arrays.asList(telegrams.toString()));
        post.setPromoSiteName(Arrays.asList(telegrams.toString()));
        post.setAuthor(null);
        post.setTimestamp(null);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setDeletedAt(null);
        post.setDeleted(false);

        postsRepository.save(post);
    } // -> 수정 예정

    // 스케줄 1
    // 1. html 전처리
    // 2. 본문에서 텔레그램 채널 추출 시
    //  2.1. 해당 채널이 DB에 있으면 홍보글 개수 업데이트
    //  2.2. 없으면 검문 서비스 호출
    public String htmlPreprocess(String url, String html) {
        String api = "http://127.0.0.1:5000/preprocess/extract/web-promotion";

        Map<String, String> requestBody = Map.of("html", html);
        // System.out.println("[DEBUG] HTML Preprocess RequestBody");
        // System.out.println(requestBody);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(api, requestBody, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) { // 다시 확인 필요
                throw new RuntimeException("\t\t[PreprocessService] HTML 전처리 실패: \" + response.getStatusCode()");
            }
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String content = jsonNode.get("promotion_content").asText(null); // null 기본값 사용 (promotion_content가 없거나 null인 경우)
            List<String> telegrams = objectMapper.convertValue(
                    jsonNode.get("telegrams"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

            // 기존 post 아이디 설정하고, 아이디로 데이터 조회하는 부분
            // String id = "post_" + url; // 아이디는 자동 생성 -> 아이디 생성 부분 삭제
            // Optional<Posts> existingEntry = postsRepository.findById(id); // 존재하지 않아야 실행되도록 구현됨 -> 삭제

            System.out.println("\t\t[PreprocessService] 전처리 결과: " + content);
            if (content == "null" || content == null) {
                System.out.println("\t\t[PreprocessService] 마약 관련 홍보글이 아닙니다!");
                /*
                if (existingEntry.isPresent()) { // DB에 존재하면
                    post = existingEntry.get();
                    post.setDeletedAt(now);
                }
                */
            } else { // 마약 관련 홍보글인 경우
                System.out.println("\t\t[PreprocessService] 마약 관련 홍보글입니다!");
                // System.out.println(content);
                // htmlCrawlingService.saveHtml(html, url); // 홍보글이라고 판단됨 -> html 저장

                saveData(html, url, content, telegrams); // 저장

                System.out.println("\t\t[PreprocessService] " + url + " saved");

                System.out.println("\t\t[PreprocessService] 추출된 채널: " + telegrams);
                if (telegrams.size() > 0) {
                    for (String telegram : telegrams) {
                        if (chInfoService.isChannelExists(telegram)) { // DB에 이미 정보가 존재하면 스킵
                            System.out.println("\t\t[PreprocessService] DB에 해당 채널이 이미 존재합니다 !");
                            ChInfo chInfo = chInfoRepository.findById(telegram).get();
                            chInfo.setPromoCount(chInfo.getPromoCount() + 1); // 테스트 필요
                        } else { // DB에 해당 채널 아이디가 존재하지 않으면 채널 검문 모듈 실행
                            channelCheckService.checkChannel(telegram);
                        }
                    }
                }
            }
            return content;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("\t\t[PreprocessService] JSON 파싱 중 오류 발생: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("\t\t[PreprocessService] HTML 전처리 중 오류 발생: " + e.getMessage(), e);
        }
    }


    // 아래는 직접 실행 (테스트용) / 업데이트 안 된 코드임
    /*
    public String postPreprocess(String url, String requestData) {
        System.out.println(requestData);

        String api = "http://127.0.0.1:5000/preprocess/extract/web-promotion";

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("html", requestData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonNode.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(api, requestEntity, String.class);

        // return response.getBody(); // 기본 반환 코드 (인코딩 문제 발생)

        LocalDateTime now = LocalDateTime.now();

        String id = "post_" + url;
        Posts posts;
        Optional<Posts> existingEntry = postsRepository.findById(response.getBody());

        if (!existingEntry.isPresent()) {
            System.out.println(id + "\tPost not found");
            posts = new Posts();
            posts.setId(id);
            posts.setLink(url);
            posts.setTag("tag");
            posts.setSiteName("site name");
            posts.setTitle("title");
            // posts.setPromoSiteLink("promotion site link");
            // posts.setPromoSiteName("promo site name");
            posts.setAuthor("author");
            posts.setTimestamp(null);
            posts.setCreatedAt(now);
        } else {
            System.out.println(id + "\tPost found");
            posts = existingEntry.get();
        }
        // posts.setContent(response.getBody());
        posts.setUpdatedAt(now);
        // postsRepository.save(posts);

        String content;
        try {
            JsonNode jsonNode2 = objectMapper.readTree(response.getBody()); // 파싱
            // 필드 추출
            content = jsonNode2.get("promotion_content").asText();
            posts.setContent(content);
            postsRepository.save(posts);
            System.out.println("Successfully saved !");
            return content;
        } catch (Exception e) {
            posts.setContent(response.getBody());
            postsRepository.save(posts);
            System.out.println("Decoding failed, saved as it.");
            throw new RuntimeException("Failed to parse response", e);
        }


    }

     */
}
