package com.team7.retriever.repository;

import com.team7.retriever.entity.ChData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChDataRepository extends MongoRepository<ChData, String> {
    
    // 유저 아이디로 조회
    List<ChData> findBySender_Id(long sender_id);

    // 유저 이름으로 조회
    List<ChData> findBySender_Name(String name);
    
    // 채널 아이디로 조회
    List<ChData> findByChannelId(long channelId);
    
    // 메시지 url로 조회
    List<ChData> findByUrl(String url);

    // 메시지 내용으로 조회 (포함되는 것)
    @Query("{ 'text':  { $regex: ?0, $options:  'i' } }")
    List<ChData> findByText(String text);
}

