package com.example.AIHealth.dto;

import lombok.Getter;
import lombok.ToString;
import java.util.Collections;
import java.util.List;

@Getter
@ToString
public class RecommendationResponse {
    private final String dietRecommendation;
    private final String exerciseRecommendation;
    private final List<MealItem> breakfastPlan;
    private final List<MealItem> lunchPlan;
    private final List<MealItem> dinnerPlan;
    private final int totalCalories;

    public RecommendationResponse(String diet, String exercise, List<MealItem> bPlan, List<MealItem> lPlan, List<MealItem> dPlan) {
        this.dietRecommendation = diet;
        this.exerciseRecommendation = exercise;
        this.breakfastPlan = bPlan;
        this.lunchPlan = lPlan;
        this.dinnerPlan = dPlan;
        this.totalCalories = bPlan.stream().mapToInt(MealItem::getCalories).sum() +
                lPlan.stream().mapToInt(MealItem::getCalories).sum() +
                dPlan.stream().mapToInt(MealItem::getCalories).sum();
    }

    public RecommendationResponse() {
        this("추천 식단 정보 없음", "추천 운동 정보 없음", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }
}