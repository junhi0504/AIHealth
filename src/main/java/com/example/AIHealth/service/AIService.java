package com.example.AIHealth.service;

import com.example.AIHealth.dto.InbodyRequest;
import com.example.AIHealth.dto.MealItem;
import com.example.AIHealth.dto.RecommendationResponse;
import com.example.AIHealth.util.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIService {

    private final OpenAIClient openAIClient;
    private final RapidApiService rapidApiService;

    @Autowired
    public AIService(OpenAIClient openAIClient, RapidApiService rapidApiService) {
        this.openAIClient = openAIClient;
        this.rapidApiService = rapidApiService;
    }

    public RecommendationResponse generateRecommendations(InbodyRequest request) {
        String userProfile = String.format(
                "신장: %.1fcm, 체중: %.1fkg, 체지방률: %.1f%%, 골격근량: %.1fkg, 내장지방레벨: %d, 목표: %s",
                request.getHeight(), request.getWeight(), request.getBodyFatPercentage(),
                request.getMuscleMass(), request.getVisceralFatLevel(), request.getGoal()
        );

        String exerciseData = rapidApiService.getExerciseData();

        String prompt = """
        다음은 사용자의 인바디 정보와 운동 데이터입니다. 이 정보를 참고하여 아래 요구사항에 맞게 맞춤형 추천을 생성해주세요.

        ✅ 출력 형식은 반드시 아래와 같이 작성하세요:
        - 사용자의 운동 목표와 현재 체형에 대해서 설명해주고 앞으로의 운동 방향성에 대해 자세하게 설명해주세요.
        [운동 추천]
        - 사용자의 체형과 체지방률에 맞는 운동 루틴을 하루 기준으로 구성해주세요.
        - 월요일부터 일요일까지 요일별로 운동 부위, 운동 세트 수, 반복 횟수, 예상 소비 칼로리를 포함해주세요.
        - **하루당 운동 수는 5개 이상으로 구성해주세요.**
        - 예시:
          월요일(가슴)
          1. 스쿼트 - 4세트 x 15회 (약 120kcal 소모)
          2. 푸쉬업 - 4세트 x 20회 (약 100kcal 소모)
          3. 벤치프레스 - 4세트 x 12회 (약 100kcal 소모)
          화요일(등)
          1. 랫풀다운 - 4세트x 12회 (약 90kcal 소모)
          2. 데드리프트 - 4세트 x 12회 (약 130kcal 소모)
          3. 시티드로우 - 4세트 x 12회 (약 80kcal 소모)
        - 전체 예상 소비 칼로리를 명시해주세요.

        [식단 추천]
        - 사용자의 인바디 정보를 바탕으로 하루 권장 섭취 칼로리를 계산해주세요.
        - 아침, 점심, 저녁 식단을 제안하고 밥 위주의 식단으로 구성해주세요.
        - **각 음식별로 섭취량(g), 칼로리, 탄수화물, 단백질, 지방을 작성해주세요.**
        - **반드시 "- 음식 이름(숫자g): 숫자kcal (탄수화물:숫자g, 단백질:숫자g, 지방:숫자g)" 형식**을 지켜주세요.
        - 예시:
          아침:
          - 오트밀(40g): 150kcal (탄수화물:27g, 단백질:5g, 지방:3g)
          - 바나나(100g): 100kcal (탄수화물:25g, 단백질:1g, 지방:0g)
          - 우유(200ml): 150kcal (탄수화물:12g, 단백질:8g, 지방:8g)
          점심:
          - 현미밥(150g): 300kcal (탄수화물:60g, 단백질:7g, 지방:2g)
          - 닭가슴살(100g): 200kcal (탄수화물:0g, 단백질:40g, 지방:3g)
          - 샐러드(100g): 100kcal (탄수화물:20g, 단백질:5g, 지방:2g)

        [사용자 인바디 정보]
        %s

        [운동 데이터 예시]
        (일부 운동 데이터)
        %s
        """.formatted(userProfile, exerciseData.substring(0, Math.min(1500, exerciseData.length())));

        String result = openAIClient.askOpenAI(prompt);

        String exercise;
        String diet;

        if (result.contains("[운동 추천]") && result.contains("[식단 추천]")) {
            int exerciseStart = result.indexOf("[운동 추천]");
            int dietStart = result.indexOf("[식단 추천]");
            exercise = result.substring(exerciseStart, dietStart).trim();
            diet = result.substring(dietStart).trim();
        } else {
            exercise = "운동 추천 정보를 생성하지 못했습니다. AI 응답: " + result;
            diet = "식단 추천 정보를 생성하지 못했습니다.";
        }

        List<MealItem> breakfastItems = parseMealItems(diet, "아침", "점심");
        List<MealItem> lunchItems = parseMealItems(diet, "점심", "저녁");
        List<MealItem> dinnerItems = parseMealItems(diet, "저녁", null);

        return new RecommendationResponse(diet, exercise, breakfastItems, lunchItems, dinnerItems);
    }

    public String chatWithAI(String userInput) {
        return openAIClient.askOpenAI(userInput);
    }

    private List<MealItem> parseMealItems(String fullText, String startKeyword, String endKeyword) {
        List<MealItem> items = new ArrayList<>();
        try {
            int startIndex = fullText.indexOf(startKeyword);
            if (startIndex == -1) return items;

            int endIndex = (endKeyword != null && fullText.indexOf(endKeyword, startIndex) != -1)
                    ? fullText.indexOf(endKeyword, startIndex)
                    : fullText.length();

            String mealBlock = fullText.substring(startIndex, endIndex);

            // Updated regex to capture serving size (g or ml)
            Pattern pattern = Pattern.compile("-\\s*(.*?)\\s*\\((\\d+)(g|ml)\\):\\s*(\\d+)\\s*kcal\\s*\\(탄수화물:\\s*(\\d+)g,\\s*단백질:\\s*(\\d+)g,\\s*지방:\\s*(\\d+)g\\)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(mealBlock);

            while (matcher.find()) {
                String foodName = matcher.group(1).trim();
                int servingSize = Integer.parseInt(matcher.group(2));
                String unit = matcher.group(3);
                int calories = Integer.parseInt(matcher.group(4));
                int carbs = Integer.parseInt(matcher.group(5));
                int protein = Integer.parseInt(matcher.group(6));
                int fat = Integer.parseInt(matcher.group(7));

                // Adjusting the display format to include serving size
                items.add(new MealItem(
                        String.format("%s (%d%s) (C:%dg, P:%dg, F:%dg)", foodName, servingSize, unit, carbs, protein, fat),
                        calories
                ));
            }
        } catch (Exception e) {}
        return items;
    }
}