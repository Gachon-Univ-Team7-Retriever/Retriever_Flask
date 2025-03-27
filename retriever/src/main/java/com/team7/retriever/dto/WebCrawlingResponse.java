package com.team7.retriever.dto;

import java.util.List;

public class WebCrawlingResponse {
    // private List<CrawlGoogleResponse> google;
    private List<String> google;
    private List<String> telegrams;

    // Getter & Setter
    public List<String> getGoogle() {
        return google;
    }

    public void setGoogle(List<String> google) {
        this.google = google;
    }

    public List<String> getTelegrams() {
        return telegrams;
    }

    public void setTelegram(List<String> telegrams) {
        this.telegrams = telegrams;
    }
}
