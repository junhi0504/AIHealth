package com.example.AIHealth.controller;

import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/save")
    public String saveForm() {
        return "save";
    }

    @PostMapping("/member/save")
    public String save(@ModelAttribute MemberDTO memberDTO) {
        memberService.save(memberDTO);
        return "redirect:/member/login";
    }

    @GetMapping("/member/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/member/update")
    public String updateForm(Authentication authentication, Model model) {
        String userEmail = authentication.getName(); // 로그인 ID(이메일)를 가져옴
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

        // ✨ [수정] 헤더에 필요한 loginMember 객체 추가
        model.addAttribute("loginMember", loginMember);
        // ✨ [수정] update.html 템플릿이 사용할 updateMember 객체 추가
        model.addAttribute("updateMember", loginMember);

        model.addAttribute("pageTitle", "정보 수정");
        model.addAttribute("contentTemplate", "update");
        return "layout";
    }

    @PostMapping("/member/update")
    public String update(@ModelAttribute MemberDTO memberDTO) {
        // ✨ [수정] DB 업데이트와 리다이렉트만 수행하도록 단순화 (세션 갱신 로직 제거)
        memberService.update(memberDTO);
        return "redirect:/main/profile";
    }

    @GetMapping("/admin/members")
    public String findAll(Authentication authentication, Model model) {
        if (authentication != null) {
            String adminEmail = authentication.getName();
            MemberDTO loginMember = memberService.findByMemberEmail(adminEmail);
            model.addAttribute("loginMember", loginMember);
        }

        List<MemberDTO> memberDTOList = memberService.findAll();
        model.addAttribute("memberList", memberDTOList);
        model.addAttribute("pageTitle", "회원 관리");
        model.addAttribute("contentTemplate", "admin/memberList");
        return "layout";
    }

    @PostMapping("/admin/members/delete/{id}")
    public String deleteById(@PathVariable Long id) {
        memberService.deleteById(id);
        return "redirect:/admin/members";
    }

    @PostMapping("/member/email-check")
    @ResponseBody
    public String emailCheck(@RequestParam("memberEmail") String memberEmail) {
        return memberService.emailCheck(memberEmail);
    }

    @PostMapping("/member/name-check")
    @ResponseBody
    public String nameCheck(@RequestParam("memberName") String memberName) {
        return memberService.nameCheck(memberName);
    }
}