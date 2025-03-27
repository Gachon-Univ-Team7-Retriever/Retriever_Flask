package com.team7.retriever;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Scheduler 활성화
public class RetrieverApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetrieverApplication.class, args);
    }


}
