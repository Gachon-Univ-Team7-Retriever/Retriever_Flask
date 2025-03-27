package com.team7.retriever.controller;

import com.team7.retriever.dto.PostLinkRequest;
import com.team7.retriever.entity.Posts;
import com.team7.retriever.service.PostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
public class PostsController {

    @Autowired
    private PostsService PostsService;

    // 전체 게시글 조회
    @GetMapping("/all")
    public List<Posts> getAllPosts() {
        return PostsService.getAllPosts();
    }
    
    /* 241231 추가 */
    // Id로 조회
    @GetMapping("/id/{id}") 
    public Optional<Posts> getPostById(@PathVariable String id) { return PostsService.getPostById(id); }

    /*
    // 특정 게시글 ID로 조회
    @GetMapping("/{id}")
    public Posts getPostById(@PathVariable String id) {
        return PostsService.getPostById(id);
    }
     */

    // 태그로 조회
    @GetMapping("/tag/{tag}")
    public List<Posts> getPostsByTag(@PathVariable String tag) {
        return PostsService.getPostsByTag(tag);
    }

    // 제목에 포함되는 것
    @GetMapping("/title/{title}")
    public List<Posts> getPostsByTitleContaining(@PathVariable String title) {
        return PostsService.getPostsByTitleContaining(title);
    }

    // 내용에 포함되는 것
    @GetMapping("/content/{content}")
    public List<Posts> getPostsByContentContaining(@PathVariable String content) {
        return PostsService.getPostsByContentContaining(content);
    }

    // 게시 사이트로 조회
    @GetMapping("/site/{siteName}")
    public List<Posts> getPostsBySiteName(@PathVariable String siteName) {
        return PostsService.getPostsBySiteName(siteName);
    }

    // promo == 타고 넘어가는 판매 사이트(채널)
    // 프로모션 링크로 조회
    @GetMapping("/promoLink/{promoSiteLink}")
    public List<Posts> getPostsByPromoSiteLink(@PathVariable String promoSiteLink) {
        return PostsService.getPostsByPromoSiteLink(promoSiteLink);
    }

    // 프로모션 이름으로 조회
    @GetMapping("/promoName/{promoSiteName}")
    public List<Posts> getPostsByPromoSiteName(@PathVariable String promoSiteName) {
        return PostsService.getPostsByPromoSiteName(promoSiteName);
    }

    // 게시글 작성자 이름으로 조회
    @GetMapping("/author/{author}")
    public List<Posts> getPostsByAuthor(@PathVariable String author) {
        return PostsService.getPostsByAuthor(author);
    }

    // 해당 링크로 조회 -> 모든 버전 조회 (수집 시점 순)
    @PostMapping("/link")
    public List<Posts> getPostsByLink(@RequestBody PostLinkRequest linkRequest) {
        return PostsService.getPostsByLink(linkRequest.getLink());
    }
}
