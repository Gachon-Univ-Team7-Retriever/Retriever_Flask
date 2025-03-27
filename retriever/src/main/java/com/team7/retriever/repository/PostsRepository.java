package com.team7.retriever.repository;

import com.team7.retriever.entity.Posts;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostsRepository extends MongoRepository<Posts, String> {

    // 전체 게시글 조회
    List<Posts> findAll();

    /* 241231 추가 */
    // Id로 조회
    Optional<Posts> findById(String id);

    // 태그로 조회
    List<Posts> findByTag(String tag);

    // 제목에 포함되는 것
    List<Posts> findByTitleContaining(String title);

    // 내용에 포함되는 것
    List<Posts> findByContentContaining(String content);

    // 게시 사이트로 조회
    List<Posts> findBySiteName(String siteName);

    // 프로모션 링크로 조회
    List<Posts> findByPromoSiteLink(String promoSiteLink);

    // 프로모션 이름으로 조회
    List<Posts> findByPromoSiteName(String promoSiteName);

    // 게시글 작성자 이름으로 조회
    List<Posts> findByAuthor(String author);

    // 게시글 링크로 조회
    List<Posts> findByLink(String link, Sort sort);
}
