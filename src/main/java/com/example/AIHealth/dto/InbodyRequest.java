package com.example.AIHealth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InbodyRequest {
    private Double height;            // primitive → Wrapper
    private Double weight;
    private Double bodyFatPercentage;
    private Double muscleMass;
    private Integer visceralFatLevel; // int → Integer
    private String goal;
    private LocalDate measurementDate;  // 측정 날짜 추가
}
