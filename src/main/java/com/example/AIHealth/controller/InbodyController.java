package com.example.AIHealth.controller;

import com.example.AIHealth.dto.InbodyRequest;
import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.dto.RecommendationResponse;
import com.example.AIHealth.entity.InbodyEntity;
import com.example.AIHealth.service.AIService;
import com.example.AIHealth.service.InbodyService;
import com.example.AIHealth.service.MemberService;
import com.example.AIHealth.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/inbody")
@RequiredArgsConstructor
public class InbodyController {

    private final AIService aiService;
    private final InbodyService inbodyService;
    private final MemberService memberService;
    private final RecommendationService recommendationService;

    /**
     * 공통 로직: 로그인 멤버 정보를 Model에 추가하고 MemberDTO 객체를 반환합니다.
     *
     * @param authentication 현재 인증된 사용자 정보
     * @param model          뷰로 데이터를 전달하는 Model 객체
     * @return 로그인 멤버의 DTO 객체
     */
    private MemberDTO addLoginMemberAndGet(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            MemberDTO loginMember = memberService.findByMemberEmail(userEmail);
            model.addAttribute("loginMember", loginMember);
            return loginMember;
        }
        return null;
    }

    /**
     * Inbody 정보 입력/수정 폼을 보여줍니다.
     */
    @GetMapping("/input")
    public String showInbodyInputForm(Authentication authentication, Model model) {
        MemberDTO loginMember = addLoginMemberAndGet(authentication, model);

        InbodyRequest inbodyRequest = new InbodyRequest();
        if (loginMember != null) {
            inbodyService.findLatestByMemberId(loginMember.getId()).ifPresent(inbody -> {
                inbodyRequest.setHeight(inbody.getHeight());
                inbodyRequest.setWeight(inbody.getWeight());
                inbodyRequest.setBodyFatPercentage(inbody.getBodyFatPercentage());
                inbodyRequest.setMuscleMass(inbody.getMuscleMass());
                inbodyRequest.setVisceralFatLevel(inbody.getVisceralFatLevel());
                inbodyRequest.setGoal(inbody.getGoal());
            });
        }
        model.addAttribute("inbodyRequest", inbodyRequest);
        model.addAttribute("pageTitle", "인바디 정보 입력/수정");
        model.addAttribute("contentTemplate", "inbody_input");
        return "layout";
    }

    /**
     * 인바디 분석 폼을 보여줍니다.
     */
    @GetMapping("/form")
    public String showInbodyForm(Authentication authentication, Model model) {
        MemberDTO loginMember = addLoginMemberAndGet(authentication, model);

        InbodyRequest inbodyRequest = new InbodyRequest();
        if (loginMember != null) {
            inbodyService.findLatestByMemberId(loginMember.getId()).ifPresent(inbody -> {
                inbodyRequest.setHeight(inbody.getHeight());
                inbodyRequest.setWeight(inbody.getWeight());
                inbodyRequest.setBodyFatPercentage(inbody.getBodyFatPercentage());
                inbodyRequest.setMuscleMass(inbody.getMuscleMass());
                inbodyRequest.setVisceralFatLevel(inbody.getVisceralFatLevel());
                inbodyRequest.setGoal(inbody.getGoal());
            });
        }
        model.addAttribute("inbodyRequest", inbodyRequest);
        model.addAttribute("pageTitle", "인바디 분석");
        model.addAttribute("contentTemplate", "inbody_form");
        return "layout";
    }

    /**
     * 분석 결과를 보여주는 페이지로 이동합니다.
     */
    @GetMapping("/result")
    public String showInbodyResult(Authentication authentication, Model model) {
        addLoginMemberAndGet(authentication, model);
        model.addAttribute("pageTitle", "분석 결과");
        model.addAttribute("contentTemplate", "inbody_result");
        return "layout";
    }

    /**
     * 인바디 데이터를 저장하거나 업데이트합니다.
     */
    @PostMapping("/save")
    public String saveOrUpdateInbody(@ModelAttribute InbodyRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);
        LocalDate today = LocalDate.now();
        inbodyService.saveOrUpdateInbodyWithDate(loginMember.getId(), request, today);
        return "redirect:/main/profile";
    }

    /**
     * 인바디 데이터를 분석하고 AI 추천 결과를 생성 및 저장합니다.
     * 500 오류를 방지하기 위해 전체 로직을 try-catch 블록으로 감싸 예외를 처리합니다.
     */
    @PostMapping("/analyze")
    public String analyzeInbody(@ModelAttribute InbodyRequest request, Authentication authentication, RedirectAttributes redirectAttributes) {
        // 사용자가 유효한지 확인합니다.
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 상태를 확인할 수 없습니다. 다시 로그인해주세요.");
            return "redirect:/member/login"; // 또는 적절한 에러 페이지
        }

        String userEmail = authentication.getName();
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

        // 로그인 멤버가 유효한지 다시 한번 확인합니다.
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원 정보를 찾을 수 없습니다. 다시 로그인해주세요.");
            return "redirect:/member/login";
        }

        try {
            LocalDate today = LocalDate.now();
            // 1. 인바디 데이터를 저장합니다.
            inbodyService.saveOrUpdateInbodyWithDate(loginMember.getId(), request, today);

            // 2. AI 분석을 요청하고 추천 결과를 받습니다.
            RecommendationResponse response = aiService.generateRecommendations(request);

            // 3. 추천 결과를 저장합니다.
            recommendationService.saveRecommendation(response, loginMember.getId());

            // 4. 분석 결과를 다음 페이지로 전달합니다.
            redirectAttributes.addFlashAttribute("recommendationResult", response);
        } catch (Exception e) {
            // AI 서비스 통신 오류, 데이터베이스 저장 오류 등 모든 예외를 여기서 처리합니다.
            redirectAttributes.addFlashAttribute("errorMessage", "분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            System.err.println("Error during inbody analysis: " + e.getMessage()); // 개발자용 로그
            return "redirect:/inbody/form"; // 오류 발생 시 입력 폼으로 돌아갑니다.
        }

        return "redirect:/inbody/result";
    }
}