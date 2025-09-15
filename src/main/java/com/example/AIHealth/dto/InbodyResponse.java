package com.example.AIHealth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class InbodyResponse {
    private Double height = 0.0;
    private Double weight = 0.0;
    private Double bodyFatPercentage = 0.0;
    private Double muscleMass = 0.0;
    private Integer visceralFatLevel = 0;
    private String goal = "정보 없음";
    private String measurementDate; // JavaScript 호환성을 위해 String 타입으로 변경

    public InbodyResponse(Double height, Double weight, Double bodyFatPercentage,
                          Double muscleMass, Integer visceralFatLevel,
                          String goal, String measurementDate) {
        this.height = (height != null) ? height : 0.0;
        this.weight = (weight != null) ? weight : 0.0;
        this.bodyFatPercentage = (bodyFatPercentage != null) ? bodyFatPercentage : 0.0;
        this.muscleMass = (muscleMass != null) ? muscleMass : 0.0;
        this.visceralFatLevel = (visceralFatLevel != null) ? visceralFatLevel : 0;
        this.goal = (goal != null) ? goal : "정보 없음";
        this.measurementDate = measurementDate;
    }
}