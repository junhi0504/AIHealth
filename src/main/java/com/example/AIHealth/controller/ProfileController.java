package com.example.AIHealth.controller;

import com.example.AIHealth.dto.InbodyResponse;
import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.dto.RecommendationResponse;
import com.example.AIHealth.service.InbodyService;
import com.example.AIHealth.service.MemberService;
import com.example.AIHealth.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // Principal 대신 사용
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/main/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final InbodyService inbodyService;
    private final RecommendationService recommendationService;
    private final MemberService memberService;

    @GetMapping
    public String profilePage(Authentication authentication, Model model) { // Principal -> Authentication
        String userEmail = authentication.getName();
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

        // ✨ [제거] 네비게이션 바에 필요한 로그인 정보는 layout.html에서 처리하므로 삭제
        // model.addAttribute("isLogin", true);
        // model.addAttribute("userName", loginMember.getMemberName());

        // ✅ [유지] 페이지 자체에서 필요한 loginMember 객체는 그대로 전달
        model.addAttribute("loginMember", loginMember);

        // 기존 비즈니스 로직
        List<InbodyResponse> inbodyList = inbodyService.getInbodyRecords(loginMember.getId());
        model.addAttribute("inbodyList", inbodyList);

        RecommendationResponse recommendation = recommendationService.getLatestRecommendation(loginMember.getId());
        if (recommendation == null) {
            recommendation = new RecommendationResponse();
        }
        model.addAttribute("recommendation", recommendation);

        // 레이아웃 설정
        model.addAttribute("pageTitle", "회원정보");
        model.addAttribute("contentTemplate", "profile");
        model.addAttribute("mainClass", "profile-main");

        return "layout";
    }

    @GetMapping("/inbodyRecords/{memberId}")
    @ResponseBody
    public List<InbodyResponse> getInbodyRecords(@PathVariable Long memberId) {
        List<InbodyResponse> list = inbodyService.getInbodyRecords(memberId);
        for (InbodyResponse r : list) {
            if (r.getHeight() == null) r.setHeight(0.0);                // ✨ 추가
            if (r.getWeight() == null) r.setWeight(0.0);
            if (r.getMuscleMass() == null) r.setMuscleMass(0.0);
            if (r.getBodyFatPercentage() == null) r.setBodyFatPercentage(0.0);
            if (r.getVisceralFatLevel() == null) r.setVisceralFatLevel(0); // ✨ 추가
            if (r.getMeasurementDate() == null) {
                r.setMeasurementDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        }
        return list;
    }


    @GetMapping("/api/recommendations/{memberId}")
    @ResponseBody
    public RecommendationResponse getRecommendations(@PathVariable Long memberId) {
        RecommendationResponse response = recommendationService.getLatestRecommendation(memberId);
        if (response == null) {
            response = new RecommendationResponse();
        }
        return response;
    }
}