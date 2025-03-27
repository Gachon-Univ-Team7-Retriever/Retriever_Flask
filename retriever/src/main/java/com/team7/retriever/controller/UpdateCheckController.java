package com.team7.retriever.controller;

import com.team7.retriever.dto.PostUpdateRequest;
import com.team7.retriever.service.PostsService;
import com.team7.retriever.service.PreprocessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post-update")
public class UpdateCheckController {
    @Autowired
    PostsService postsService;
    @Autowired
    private PreprocessService preprocessService;

    // DB 데이터 조회 결과 테스트
    @GetMapping("/posts")
    public List<String> getPosts() {
        return postsService.getAllPostsForUpdate();
    }

    // Post 업데이트 테스트
    @PostMapping("/test")
    public String updateTest(@RequestBody PostUpdateRequest postUpdateRequest) {
        String html = postUpdateRequest.getHtml();
        String link = postUpdateRequest.getLink();

        return preprocessService.updatePreprocess(html, link);
    }
}
