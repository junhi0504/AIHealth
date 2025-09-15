package com.example.AIHealth.controller;

import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.dto.RecommendationResponse;
import com.example.AIHealth.entity.RecommendationEntity;
import com.example.AIHealth.service.MemberService;
import com.example.AIHealth.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Authentication 임포트
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final MemberService memberService;

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveRecommendation(@RequestBody RecommendationResponse dto, Authentication authentication) { // Principal -> Authentication
        String userEmail = authentication.getName();
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

        recommendationService.saveRecommendation(dto, loginMember.getId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/view")
    public String viewRecommendation(Model model, Authentication authentication) { // Principal -> Authentication
        String userEmail = authentication.getName();
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

        // 헤더를 위해 loginMember를 모델에 추가
        model.addAttribute("loginMember", loginMember);

        RecommendationEntity recommendation = recommendationService.getLatestRecommendationForCurrentUser(loginMember.getId());

        if (recommendation != null) {
            String diet = recommendation.getDietRecommendation();
            String exercise = recommendation.getExerciseRecommendation();
            String todayExercise = extractTodayExercise(exercise);

            model.addAttribute("diet", diet);
            model.addAttribute("todayExercise", todayExercise);
        } else {
            // 빈 객체를 보내 템플릿 오류 방지
            model.addAttribute("diet", "추천 식단 정보가 없습니다.");
            model.addAttribute("todayExercise", "추천 운동 정보가 없습니다.");
        }

        // "nickname" 대신 이미 "loginMember"를 전달했으므로 이 줄은 제거 가능
        // model.addAttribute("nickname", loginMember.getMemberName());

        // 이 메소드가 실제로 profile.html을 렌더링하는지는 불확실하나,
        // 만약 그렇다면 contentTemplate과 pageTitle도 설정해야 합니다.
        model.addAttribute("pageTitle", "추천 보기");
        model.addAttribute("contentTemplate", "profile"); // profile.html을 사용한다고 가정

        return "layout"; // "profile" 대신 "layout" 반환
    }

    @GetMapping("/latest/{memberId}")
    @ResponseBody
    public ResponseEntity<RecommendationResponse> getLatestRecommendation(@PathVariable Long memberId) {
        RecommendationResponse response = recommendationService.getLatestRecommendation(memberId);
        if (response == null) {
            response = new RecommendationResponse();
        }
        return ResponseEntity.ok(response);
    }

    private String extractTodayExercise(String fullText) {
        if (fullText == null || fullText.isEmpty()) return "오늘 운동 정보 없음";

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String todayStr = switch (today) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            case SATURDAY -> "토요일";
            case SUNDAY -> "일요일";
        };

        int start = fullText.indexOf(todayStr);
        if (start == -1) return "오늘 운동 정보 없음";

        int end = fullText.length();
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day == today) continue;
            String otherDayStr = switch (day) {
                case MONDAY -> "월요일";
                case TUESDAY -> "화요일";
                case WEDNESDAY -> "수요일";
                case THURSDAY -> "목요일";
                case FRIDAY -> "금요일";
                case SATURDAY -> "토요일";
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