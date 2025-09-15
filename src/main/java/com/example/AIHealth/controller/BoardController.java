package com.example.AIHealth.controller;

import com.example.AIHealth.dto.BoardDTO;
import com.example.AIHealth.dto.CommentDTO;
import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.entity.Role;
import com.example.AIHealth.service.BoardService;
import com.example.AIHealth.service.CommentService;
import com.example.AIHealth.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;
    private final MemberService memberService;

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "sort", defaultValue = "latest") String sort,
                       Principal principal, Model model) {

        if (principal != null) {
            MemberDTO loginMember = memberService.findByMemberEmail(principal.getName());
            model.addAttribute("isLogin", true);
            model.addAttribute("loginMember", loginMember);
        } else {
            model.addAttribute("isLogin", false);
        }

        int pageSize = 15;
        Page<BoardDTO> boardPage = boardService.getBoardPageWithCommentCountAndSort(keyword, page, pageSize, sort);

        model.addAttribute("boardPage", boardPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("pageTitle", "자유게시판");
        model.addAttribute("contentTemplate", "board/boardList");
        model.addAttribute("mainClass", "main-full-width");
        return "layout";
    }

    @GetMapping("/write")
    public String writeForm(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        String userEmail = principal.getName();
        MemberDTO loginMember = memberService.findByMemberEmail(userEmail);

        model.addAttribute("isLogin", true);
        model.addAttribute("loginMember", loginMember);

        model.addAttribute("pageTitle", "게시글 작성");
        model.addAttribute("contentTemplate", "board/boardWrite");
        return "layout";
    }

    @PostMapping("/write")
    public String write(@ModelAttribute BoardDTO boardDTO,
                        @RequestParam("imageData") String imageData,
                        Principal principal) {

        if (principal == null) {
            return "redirect:/member/login";
        }

        MemberDTO loginMember = memberService.findByMemberEmail(principal.getName());

        try {
            boardService.saveWithBase64Images(boardDTO, loginMember.getId(), imageData);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/board?error=uploadFail";
        }

        return "redirect:/board";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        MemberDTO loginMember = null;
        boolean isOwner = false;

        if (principal != null) {
            String userEmail = principal.getName();
            loginMember = memberService.findByMemberEmail(userEmail);
            model.addAttribute("isLogin", true);
            model.addAttribute("loginMember", loginMember);
        } else {
            model.addAttribute("isLogin", false);
        }

        BoardDTO board = boardService.findById(id);

        if (board != null && loginMember != null) {
            isOwner = loginMember.getRole() == Role.ADMIN || (board.getMemberId() != null && board.getMemberId().equals(loginMember.getId()));
        }

        List<CommentDTO> commentList = commentService.getCommentsByBoardId(id);

        model.addAttribute("board", board);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("commentList", commentList);
        model.addAttribute("pageTitle", board.getTitle());
        model.addAttribute("contentTemplate", "board/boardDetail");
        model.addAttribute("mainClass", "main-full-width");
        return "layout";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        MemberDTO loginMember = memberService.findByMemberEmail(principal.getName());
        model.addAttribute("isLogin", true);
        model.addAttribute("loginMember", loginMember);

        BoardDTO boardDTO;
        try {
            boardDTO = boardService.findById(id);
        } catch (IllegalArgumentException e) {
            return "redirect:/board?error=notFound";
        }

        model.addAttribute("board", boardDTO);
        model.addAttribute("pageTitle", "글 수정");
        model.addAttribute("contentTemplate", "board/boardEdit");
        return "layout";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @ModelAttribute BoardDTO boardDTO,
                       @RequestParam(value = "imageData", required = false) String imageData,
                       @RequestParam(value = "deleteImageIdsString", required = false) String deleteImageIds) {
        try {
            boardService.updateWithBase64Images(id, boardDTO.getTitle(), boardDTO.getContent(), imageData, deleteImageIds);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/board/edit/" + id + "?error=editFail";
        }
        return "redirect:/board/" + id;
    }


    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @boardService.findById(#id).getMemberId().equals(@memberService.findByMemberEmail(principal.name).getId())")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/board";
    }

    @PostMapping("/{id}/recommend")
    @ResponseBody
    public ResponseEntity<?> toggleRecommend(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        MemberDTO loginMember = memberService.findByMemberEmail(principal.getName());
        int newCount = boardService.toggleRecommend(id, loginMember.getId());

        return ResponseEntity.ok(Map.of("recommendCount", newCount));
    }
}