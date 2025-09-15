package com.example.AIHealth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 모든 필드를 받는 생성자를 만듭니다.
public class MealItem {
    private String foodName;
    private int calories;
}