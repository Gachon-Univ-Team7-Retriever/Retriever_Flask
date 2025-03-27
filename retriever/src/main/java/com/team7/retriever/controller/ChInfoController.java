package com.team7.retriever.controller;


import com.team7.retriever.entity.ChInfo;
import com.team7.retriever.service.ChInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/channels")
public class ChInfoController {
    @Autowired
    private ChInfoService infoService;

    // api test (테스트용 - 채널 정보 직접 생성, 아래 데이터)
    /*
    @PostMapping("/addChInfo")
    public String addChInfo(@RequestBody ChInfo chInfo) {
        LocalDateTime date = LocalDateTime.now();
        infoService.addChInfo("api test", "api test updated name", "api test link", "this is api test", date, date, date);
        return "success add ch info";
    }

     */

    // 전체 채널 조회
    @GetMapping("/all")
    public List<ChInfo> getAllChannelInfo() {
        if (infoService != null) {
            return infoService.getAllChannelInfo();
        } else {
            return null;
        }
    }
    // ?? null 값 처리 안 했더니 오류 발생

    // 특정 아이디로 채널 정보 조회
    @GetMapping("/id/{id}") /* 241231 수정 */
    public Optional<ChInfo> getChannelInfoById(@PathVariable String id) {
        return infoService.getChannelInfoById(id);
    }

    // 채널 링크로 채널 정보 조회
    @GetMapping("/by-link/{link}")
    public Optional<ChInfo> getChannelByLink(@PathVariable String link) {
        return infoService.getChannelByLink(link);
    }

    /* 250102 추가 */
    // 채널 이름에 포함되는 것
    @GetMapping("/name/{name}")
    public List<ChInfo> getChannelByNameContaining(@PathVariable String name) {
        return infoService.getChannelInfoByNameContaining(name);
    }
}
