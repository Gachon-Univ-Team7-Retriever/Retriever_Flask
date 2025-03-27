package com.team7.retriever.service;


import com.team7.retriever.entity.Slangs;
import com.team7.retriever.repository.SlangsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SlangsService {

    @Autowired
    private SlangsRepository slangsRepository;

    // 아이디로 조회
    public Optional<Slangs> getSlangById(String id) {
        return slangsRepository.findById(id);
    }

    // slang으로 조회
    public List<Slangs> getSlangBySlang(String slang) {
        return slangsRepository.findBySlang(slang);
    }

    // 전체 조회
    public List<Slangs> getAllSlangs() {
        return slangsRepository.findAll();
    }

    // count 많은 순으로 조회
    public List<Slangs> getSlangsSortedByCount() {
        return slangsRepository.findAllByOrderByCountDesc();
    }

    // DB에서 slang 리스트로 받아오기
    public List<String> getAllSlangsToList() {
        return slangsRepository.findAll() // DB에서 전체 조회
                .stream()
                .map(Slangs::getSlang) // slang 필드만 추출
                .filter(slang -> slang != null) // slang이 null이 아닌 경우만
                .map(slang -> "t.me " + slang) // 각 slang 앞에 t.me 추가
                .collect(Collectors.toList()); // 리스트로 변환
    }
}