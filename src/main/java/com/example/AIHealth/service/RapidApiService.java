package com.example.AIHealth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RapidApiService {

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.host}")
    private String apiHost;

    private final RestTemplate restTemplate;

    public RapidApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getExerciseData() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", apiKey);
        headers.set("X-RapidAPI-Host", apiHost);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://exercisedb.p.rapidapi.com/exercises?limit=20";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "[]"; // 빈 배열 반환
        }
    }
}
