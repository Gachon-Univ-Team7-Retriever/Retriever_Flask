package com.team7.retriever.service;

// import com.team7.retriever.dto.CrawlGoogleResponse;
import com.team7.retriever.dto.WebCrawlingRequest;
import com.team7.retriever.dto.WebCrawlingResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/*
    @Service(스프링 빈에 등록된 클래스) 라서
    어노테이션 내부에 이미 @Component가 포함되어 있기 때문에
    @Component는 따로 추가하지 않아도 된다 고 한다
( @Component는 스케줄러를 적용할 대상 클래스에 추가하는 것임 )
*/
@Service
public class WebCrawlingService {
    private final RestTemplate restTemplate;
    private final HtmlCrawlingService htmlCrawlingService;
    private final PreprocessService preprocessService;
    private final ChannelCheckService channelCheckService;
    private final PostHtmlService postHtmlService;
    private final ChInfoService chInfoService;
    private final SlangsService slangsService;

    public WebCrawlingService(RestTemplate restTemplate, HtmlCrawlingService htmlCrawlingService, PreprocessService preprocessService, ChannelCheckService channelCheckService, PostHtmlService postHtmlService, ChInfoService chInfoService, SlangsService slangsService) {
        this.restTemplate = restTemplate;
        this.htmlCrawlingService = htmlCrawlingService;
        this.preprocessService = preprocessService;
        this.channelCheckService = channelCheckService;
        this.postHtmlService = postHtmlService;
        this.chInfoService = chInfoService;
        this.slangsService = slangsService;
    }

    // 초(0-59) 분(0-59) 시간(0-23) 일(1-31) 월(1-12) 요일(0-6) (0: 일, 1: 월, 2:화, 3:수, 4:목, 5:금, 6:토)
    // initialDelay = 5000 -> 초기 5초 지연 시간 설정 -> 스케줄 안에 같이 넣는 것 (참고 용으로 기록)
    // @Scheduled(fixedDelay = 120000) // 테스트 용 - 한 사이클 종료 후 2분 지연 실행
    @Scheduled(cron = "0 0 5 * * *") // 매일 오전 5시마다 실행
    public WebCrawlingResponse webCrawling() {
        String api = "http://127.0.0.1:5000/crawl/links";

        WebCrawlingRequest webCrawlingRequest = new WebCrawlingRequest();
        webCrawlingRequest.setQueries(slangsService.getAllSlangsToList()); // 테스트 필요
        webCrawlingRequest.setMax(5);

        HttpEntity<WebCrawlingRequest> request = new HttpEntity<>(webCrawlingRequest);
        ResponseEntity<WebCrawlingResponse> response = restTemplate.postForEntity(api, request, WebCrawlingResponse.class);

        WebCrawlingResponse results = response.getBody();
        if (results != null) {
            List<String> google = results.getGoogle();
            List<String> telegram = results.getTelegrams();

            // int inserted = 0;
            // int updated = 0;

            // google
            System.out.println("[WebCrawlingService] Google --------------");
            if (google != null) {
                for (String googleResponse : google) {
                    System.out.println("- " + googleResponse);
                    // 해당 링크가 DB에 있는지 체크
                    if (postHtmlService.isUrlExists(googleResponse)) { // DB에 이미 존재하면 skip
                        System.out.println("[WebCrawlingService] DB에 해당 게시물이 이미 존재합니다 !");
                    } else { // DB에 존재하지 않으면 html 크롤링 시작
                        // 홍보글 내용 추출
                        String html = htmlCrawlingService.crawlHtml(googleResponse); // 크롤링 결과 받아옴
                        if (html != null) {
                            System.out.println("[WebCrawlingService] 내용 추출 완료");
                            // System.out.println("\t\t- " + html);
                            preprocessService.htmlPreprocess(googleResponse, html); // 전처리 실행
                            System.out.println("[WebCrawlingService] 전처리 모듈 실행 완료");
                        } else {
                            System.out.println("[WebCrawlingService] 내용 추출 실패");
                        }
                    }
                    System.out.println("[WebCrawlingService] 크롤링 실행 완료");

                    
                    // WebCrawlingService가 전체적인 흐름을 관리하도록 한다.
                    // 링크 -> html -> 전처리
                }
            } else {
                System.out.println("[WebCrawlingService] No google found"); // 결과 자체가 null이 아니라 []을 반환하는 건지?
            }

            // telegram
            System.out.println("[WebCrawlingService] Telegrams --------------");
            if (telegram != null) {
                for (String telegramResponse : telegram) {
                    System.out.println("\t- " + telegramResponse);
                    // if channel check
                        // channel scrape service call
                    // else
                    if (chInfoService.isChannelExists(telegramResponse)) { // DB에 이미 정보가 존재하면 스킵
                        System.out.println("[WebCrawlingService] DB에 해당 채널이 이미 존재합니다 !");
                    } else { // DB에 해당 채널 아이디가 존재하지 않으면 채널 검문 모듈 실행
                        channelCheckService.checkChannel(telegramResponse);
                    }
                }
            } else {
                System.out.println("[WebCrawlingService] No telegram found");
            }
            System.out.println("[WebCrawlingService] 모든 결과에 대한 처리 완료 -------------------------------");
        }


        return response.getBody();
    }

    /*
    // 클라이언트로부터 직접 바디 받음
    public WebCrawlingResponse webCrawlingTest(WebCrawlingRequest webCrawlingRequest) {
        String api = "http://127.0.0.1:5000/crawl/links";

        HttpEntity<WebCrawlingRequest> request = new HttpEntity<>(webCrawlingRequest);
        ResponseEntity<WebCrawlingResponse> response = restTemplate.postForEntity(api, request, WebCrawlingResponse.class);

        WebCrawlingResponse results = response.getBody();
        if (results != null) {
            List<String> google = results.getGoogle();
            List<String> telegram = results.getTelegrams();

            // int inserted = 0;
            // int updated = 0;

            // google
            System.out.println("[WebCrawlingService] Google --------------");
            if (google != null) {
                for (String googleResponse : google) {
                    System.out.println("- " + googleResponse);
                    // 홍보글 내용 추출
                    String html = htmlCrawlingService.crawlHtml(googleResponse);
                    // String html = htmlCrawlingService.crawlHtml("http://xn--on3b97gmrdt6b5c503hmga.com/bbs/board.php?bo_table=sh_board&wr_id=3025");
                    if (html != null) {
                        System.out.println("[WebCrawlingService] 내용 추출 완료");
                        // System.out.println("\t\t- " + html);
                        preprocessService.htmlPreprocess(googleResponse, html);
                        System.out.println("[WebCrawlingService] 전처리 모듈 실행 완료");
                    } else {
                        System.out.println("[WebCrawlingService] 내용 추출 실패");
                    }

                    // WebCrawlingService가 전체적인 흐름을 관리하도록 한다.
                    // 링크 -> html -> 전처리
                    // 저장은 각 서비스에서 ?
                    // 저장 내용 출력은 각 서비스에서,
                    // 저장 또는 실행 결과는 여기에서 출력하도록 한다.
                }
            } else {
                System.out.println("[WebCrawlingService] No google found"); // 결과 자체가 null이 아니라 []을 반환하는 건지?
            }

            // telegram
            System.out.println("[WebCrawlingService] Telegrams --------------");
            if (telegram != null) {
                for (String telegramResponse : telegram) {
                    System.out.println("\t- " + telegramResponse);
                    // if channel check
                    // channel scrape service call
                    // else
                    channelCheckService.checkChannel(telegramResponse);
                }
            } else {
                System.out.println("[WebCrawlingService] No telegram found");
            }
            System.out.println("[WebCrawlingService] 모든 결과에 대한 처리 완료 -------------------------------");
        }


        return response.getBody();
    }

     */
}
