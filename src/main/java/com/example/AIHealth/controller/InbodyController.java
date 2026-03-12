package com.example.AIHealth.controller;

import com.example.AIHealth.dto.InbodyRequest;
import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.dto.RecommendationResponse;
import com.example.AIHealth.service.AIService;
import com.example.AIHealth.service.InbodyService;
import com.example.AIHealth.service.MemberService;
import com.example.AIHealth.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/inbody")
@RequiredArgsConstructor
public class InbodyController {

    private final AIService aiService;
    private final InbodyService inbodyService;
    private final MemberService memberService;
    private final RecommendationService recommendationService;

    /** 로그인 멤버 정보 Model에 추가 */
    private MemberDTO addLoginMemberAndGet(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            MemberDTO loginMember = memberService.findByMemberEmail(userEmail);
            model.addAttribute("loginMember", loginMember);
            return loginMember;
        }
        return null;
    }

    /** 인바디 입력/수정 폼 */
    @GetMapping("/input")
    public String showInbodyInputForm(Authentication authentication, Model model) {
        MemberDTO loginMember = addLoginMemberAndGet(authentication, model);

        InbodyRequest inbodyRequest = new InbodyRequest();
        if (loginMember != null) {
            inbodyService.findLatestByMemberId(loginMember.getId())
                    .ifPresentOrElse(
                            inbody -> {
                                inbodyRequest.setHeight(inbody.getHeight());
                                inbodyRequest.setWeight(inbody.getWeight());
                                inbodyRequest.setBodyFatPercentage(inbody.getBodyFatPercentage());
                                inbodyRequest.setMuscleMass(inbody.getMuscleMass());
                                inbodyRequest.setVisceralFatLevel(inbody.getVisceralFatLevel());
                                inbodyRequest.setGoal(inbody.getGoal());
                                inbodyRequest.setMeasurementDate(inbody.getMeasurementDate());
                            },
                            () -> inbodyRequest.setMeasurementDate(LocalDate.now())
                    );
        } else {
            inbodyRequest.setMeasurementDate(LocalDate.now());
        }

        model.addAttribute("inbodyRequest", inbodyRequest);
        model.addAttribute("pageTitle", "인바디 정보 입력/수정");
        model.addAttribute("contentTemplate", "inbody_input");
        return "layout";
    }

    /** 인바디 분석 폼 */
    @GetMapping("/form")
    public String showInbodyForm(Authentication authentication, Model model) {
        MemberDTO loginMember = addLoginMemberAndGet(authentication, model);

        InbodyRequest inbodyRequest = new InbodyRequest();
        if (loginMember != null) {
            inbodyService.findLatestByMemberId(loginMember.getId())
                    .ifPresentOrElse(
                            inbody -> {
                                inbodyRequest.setHeight(inbody.getHeight());
                                inbodyRequest.setWeight(inbody.getWeight());
                                inbodyRequest.setBodyFatPercentage(inbody.getBodyFatPercentage());
                                inbodyRequest.setMuscleMass(inbody.getMuscleMass());
                                inbodyRequest.setVisceralFatLevel(inbody.getVisceralFatLevel());
                                inbodyRequest.setGoal(inbody.getGoal());
                                inbodyRequest.setMeasurementDate(inbody.getMeasurementDate());
                            },
                            () -> inbodyRequest.setMeasurementDate(LocalDate.now())
                    );
        } else {
            inbodyRequest.setMeasurementDate(LocalDate.now());
        }

        model.addAttribute("inbodyRequest", inbodyRequest);
        model.addAttribute("pageTitle", "인바디 분석");
        model.addAttribute("contentTemplate", "inbody_form");
        return "layout";
    }

    /** AJAX API: 날짜별 인바디 데이터 조회 */
    @GetMapping("/api/data")
    @ResponseBody
    public ResponseEntity<InbodyRequest> getInbodyData(@RequestParam("date") String date,
                                                       Authentication authentication) {
        MemberDTO loginMember = memberService.findByMemberEmail(authentication.getName());
        LocalDate selectedDate = LocalDate.parse(date);

        return inbodyService.findByMemberIdAndDate(loginMember.getId(), selectedDate)
                .map(inbody -> {
                    InbodyRequest req = new InbodyRequest();
                    req.setHeight(inbody.getHeight());
                    req.setWeight(inbody.getWeight());
                    req.setBodyFatPercentage(inbody.getBodyFatPercentage());
                    req.setMuscleMass(inbody.getMuscleMass());
                    req.setVisceralFatLevel(inbody.getVisceralFatLevel());
                    req.setGoal(inbody.getGoal());
                    req.setMeasurementDate(inbody.getMeasurementDate());
                    return ResponseEntity.ok(req);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    /** 분석 결과 페이지 */
    @GetMapping("/result")
    public String showInbodyResult(Authentication authentication, Model model) {
        addLoginMemberAndGet(authentication, model);
        model.addAttribute("pageTitle", "분석 결과");
        model.addAttribute("contentTemplate", "inbody_result");
        return "layout";
    }

    /** 인바디 데이터 저장 */
    @PostMapping("/save")
    public String saveOrUpdateInbody(@ModelAttribute InbodyRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

        if (loginMember != null) {
            LocalDate measurementDate = request.getMeasurementDate() != null
                    ? request.getMeasurementDate()
                    : LocalDate.now();

            inbodyService.saveOrUpdateInbodyWithDate(loginMember.getId(), request, measurementDate);
        }

        return "redirect:/main/profile";
    }

    /** 인바디 분석 + AI 추천 */
    @PostMapping("/analyze")
    public String analyzeInbody(@ModelAttribute InbodyRequest request,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 상태를 확인할 수 없습니다. 다시 로그인해주세요.");
            return "redirect:/member/login";
        }

        String userEmail = authentication.getName();
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원 정보를 찾을 수 없습니다. 다시 로그인해주세요.");
            return "redirect:/member/login";
        }

        try {
            LocalDate measurementDate = request.getMeasurementDate() != null
                    ? request.getMeasurementDate()
                    : LocalDate.now();

            inbodyService.saveOrUpdateInbodyWithDate(loginMember.getId(), request, measurementDate);
            RecommendationResponse response = aiService.generateRecommendations(request);
            recommendationService.saveRecommendation(response, loginMember.getId());

            redirectAttributes.addFlashAttribute("recommendationResult", response);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "분석 중 오류가 발생했습니다.");
            return "redirect:/inbody/form";
        }

        return "redirect:/inbody/result";
    }
}
