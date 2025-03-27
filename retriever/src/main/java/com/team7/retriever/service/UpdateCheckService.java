package com.team7.retriever.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UpdateCheckService {
    private final PostsService postsService;
    private final HtmlCrawlingService htmlCrawlingService;
    private final PreprocessService preprocessService;

    public UpdateCheckService(PostsService postsService, HtmlCrawlingService htmlCrawlingService, PreprocessService preprocessService) {
        this.postsService = postsService;
        this.htmlCrawlingService = htmlCrawlingService;
        this.preprocessService = preprocessService;
    }

    public void getAllPost() {
        /*
        List<String> allLinks = postsService.getAllPosts().stream()
                .filter(post -> !post.isDeleted())
                .map(Posts::getLink)
                .toList();
        */

        List<String> allLinks = postsService.getAllPostsForUpdate();

        for (String link : allLinks) {
            updatePost(link);
        }
    }

    public void updatePost(String link) {
        System.out.println("[UpDateCheckService] 크롤링 시작 / link: " + link);
        String newHtml = htmlCrawlingService.crawlHtml(link);
        if (newHtml != null) {
            System.out.println("[UpDateCheckService] 내용 추출 완료");
            preprocessService.updatePreprocess(newHtml, link);
            System.out.println("[UpDateCheckService] 실행 완료");
        } else {
            System.out.println("[UpDateCheckService] HTML 크롤링 결과 없음");
        }
    }
}
