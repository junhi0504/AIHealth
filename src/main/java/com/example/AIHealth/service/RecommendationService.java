package com.example.AIHealth.service;

import com.example.AIHealth.dto.MealItem;
import com.example.AIHealth.dto.RecommendationResponse;
import com.example.AIHealth.entity.MemberEntity;
import com.example.AIHealth.entity.RecommendationEntity;
import com.example.AIHealth.repository.MemberRepository;
import com.example.AIHealth.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Transactional 임포트

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final MemberRepository memberRepository;
    private final WorkoutService workoutService;

    // ✨ [수정] @Transactional 추가
    @Transactional
    public void saveRecommendation(RecommendationResponse dto, Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("해당 ID의 사용자가 존재하지 않습니다."));

        RecommendationEntity entity = new RecommendationEntity();
        entity.setExerciseRecommendation(dto.getExerciseRecommendation());
        entity.setDietRecommendation(dto.getDietRecommendation());
        entity.setMember(member);

        int bCal = dto.getBreakfastPlan().stream().mapToInt(MealItem::getCalories).sum();
        int lCal = dto.getLunchPlan().stream().mapToInt(MealItem::getCalories).sum();
        int dCal = dto.getDinnerPlan().stream().mapToInt(MealItem::getCalories).sum();

        entity.setBreakfastCalories(bCal);
        entity.setLunchCalories(lCal);
        entity.setDinnerCalories(dCal);

        recommendationRepository.save(entity);

        // ✨ [추가] 디버깅 로그
        System.out.println("\n===== [RecommendationService] WorkoutService 호출 시도 =====");
        System.out.println("운동 추천 내용: " + dto.getExerciseRecommendation());
        System.out.println("=========================================================\n");

        if (dto.getExerciseRecommendation() != null && !dto.getExerciseRecommendation().isBlank()) {
            workoutService.saveWorkoutsFromRecommendation(dto.getExerciseRecommendation(), member.getId());
        }
    }

    // ... 이하 다른 메소드들은 그대로 유지 ...
    public RecommendationEntity getLatestRecommendationForCurrentUser(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId).orElse(null);
        if (member == null) return null;

        return recommendationRepository.findTopByMemberOrderByIdDesc(member).orElse(null);
    }

    public RecommendationResponse getLatestRecommendation(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        RecommendationEntity latest = recommendationRepository.findTopByMemberOrderByIdDesc(member)
                .orElse(null);

        if (latest == null) {
            return null;
        }

        String fullDiet = latest.getDietRecommendation();
        List<MealItem> breakfastItems = parseMealItems(fullDiet, "아침", "점심");
        List<MealItem> lunchItems = parseMealItems(fullDiet, "점심", "저녁");
        List<MealItem> dinnerItems = parseMealItems(fullDiet, "저녁", null);
        String todayExercise = extractTodayExercise(latest.getExerciseRecommendation());

        return new RecommendationResponse(fullDiet, todayExercise, breakfastItems, lunchItems, dinnerItems);
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
            Pattern pattern = Pattern.compile("-\\s*(.*?):\\s*(\\d+)\\s*kcal", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(mealBlock);
            while (matcher.find()) {
                String foodName = matcher.group(1).trim();
                int calories = Integer.parseInt(matcher.group(2));
                items.add(new MealItem(foodName, calories));
            }
        } catch (Exception e) {}
        return items;
    }

    public String extractTodayExercise(String fullText) {
        if (fullText == null || fullText.isEmpty()) return "오늘 운동 정보 없음";
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String todayStr = switch (today) {
            case MONDAY -> "월요일"; case TUESDAY -> "화요일"; case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일"; case FRIDAY -> "금요일"; case SATURDAY -> "토요일";
            case SUNDAY -> "일요일";
        };
        int start = fullText.indexOf(todayStr);
        if (start == -1) return "오늘 운동 정보 없음";
        int end = fullText.length();
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day == today) continue;
            String otherDayStr = switch (day) {
                case MONDAY -> "월요일"; case TUESDAY -> "화요일"; case WEDNESDAY -> "수요일";
                case THURSDAY -> "목요일"; case FRIDAY -> "금요일"; case SATURDAY -> "토요일";
                case SUNDAY -> "일요일";
            };
            int idx = fullText.indexOf(otherDayStr, start + 1);
            if (idx != -1 && idx < end) {
                end = idx;
            }
        }
        return fullText.substring(start, end).trim();
    }
}