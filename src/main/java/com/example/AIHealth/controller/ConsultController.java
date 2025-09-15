package com.example.AIHealth.controller;

import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.service.AIService;
import com.example.AIHealth.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // Authentication 임포트
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/consult")
public class ConsultController {

    private final AIService aiService;
    private final MemberService memberService;

    @GetMapping
    public String showConsultPage(Authentication authentication, Model model) { // Principal -> Authentication
        if (authentication != null) {
            String userEmail = authentication.getName();
            MemberDTO loginMember = memberService.findByMemberEmail(userEmail);
            model.addAttribute("loginMember", loginMember); // loginMember 추가
        }

        model.addAttribute("pageTitle", "AI 상담");
        model.addAttribute("contentTemplate", "consult");
        return "layout";
    }

    @PostMapping("/chat")
    @ResponseBody
    public String getChatResponse(@RequestBody String userInput) {
        return aiService.chatWithAI(userInput);
    }
}