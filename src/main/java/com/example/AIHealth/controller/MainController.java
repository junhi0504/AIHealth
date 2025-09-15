package com.example.AIHealth.controller;

import com.example.AIHealth.dto.BoardDTO;
import com.example.AIHealth.dto.InbodyResponse;
import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.dto.RecommendationResponse;
import com.example.AIHealth.dto.WorkoutDTO; // WorkoutDTO 임포트
import com.example.AIHealth.service.BoardService;
import com.example.AIHealth.service.InbodyService;
import com.example.AIHealth.service.MemberService;
import com.example.AIHealth.service.RecommendationService;
import com.example.AIHealth.service.WorkoutService; // WorkoutService 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate; // LocalDate 임포트
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final BoardService boardService;
    private final InbodyService inbodyService;
    private final RecommendationService recommendationService;
    private final MemberService memberService;
    private final WorkoutService workoutService; // ✨ WorkoutService 주입

    @GetMapping("/main")
    public String mainPage(Authentication authentication, Model model) {

        if (authentication != null && authentication.isAuthenticated()) {
            // --- 로그인 사용자일 경우 ---
            String userEmail = authentication.getName();
            MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

            model.addAttribute("loginMember", loginMember);

            // 사용자 데이터 조회 (기존 로직)
            List<InbodyResponse> inbodyList = inbodyService.getInbodyRecords(loginMember.getId());
            model.addAttribute("inbodyList", inbodyList);

            RecommendationResponse recommendation = recommendationService.getLatestRecommendation(loginMember.getId());
            if (recommendation == null) {
                recommendation = new RecommendationResponse();
            }
            model.addAttribute("recommendation", recommendation);

            // ✨ [추가] 오늘 날짜의 운동 기록 조회
            List<WorkoutDTO> todayWorkouts = workoutService.getWorkoutByDateForMember(loginMember.getId(), LocalDate.now());
            model.addAttribute("todayWorkouts", todayWorkouts);


            model.addAttribute("contentTemplate", "mainpage_user");

        } else {
            // --- 비로그인 사용자일 경우 ---
            model.addAttribute("contentTemplate", "mainpage_guest");
        }

        // 자유게시판 최신글은 공통으로 조회
        List<BoardDTO> recentPosts = boardService.findRecentPosts(3);
        model.addAttribute("recentPosts", recentPosts);

        model.addAttribute("pageTitle", "메인 페이지");

        return "layout";
    }
}