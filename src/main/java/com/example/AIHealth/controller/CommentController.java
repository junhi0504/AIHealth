package com.example.AIHealth.controller;

import com.example.AIHealth.dto.CommentDTO;
import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.service.CommentService;
import com.example.AIHealth.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // Authentication 임포트
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;
    private final MemberService memberService;

    @PostMapping("/save")
    public String save(@ModelAttribute CommentDTO dto, Authentication authentication) { // Principal -> Authentication
        String userEmail = authentication.getName();
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

        commentService.saveComment(dto, loginMember.getId());

        return "redirect:/board/" + dto.getBoardId();
    }

    @PostMapping("/delete/{id}")
    public String deleteComment(@PathVariable Long id, Authentication authentication) { // Principal -> Authentication
        String userEmail = authentication.getName();
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

        Long boardId = commentService.getBoardIdByCommentId(id);

        commentService.deleteComment(id, loginMember);

        return "redirect:/board/" + boardId;
    }

}