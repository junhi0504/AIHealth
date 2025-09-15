package com.example.AIHealth.service;

import com.example.AIHealth.dto.CommentDTO;
import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.entity.BoardEntity;
import com.example.AIHealth.entity.CommentEntity;
import com.example.AIHealth.entity.MemberEntity;
import com.example.AIHealth.entity.Role;
import com.example.AIHealth.mapper.CommentMapper;
import com.example.AIHealth.repository.BoardRepository;
import com.example.AIHealth.repository.CommentRepository;
import com.example.AIHealth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Use Spring's Transactional

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public void saveComment(CommentDTO dto, Long memberId) {
        BoardEntity board = boardRepository.findById(dto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        CommentEntity comment = CommentEntity.builder()
                .content(dto.getContent())
                .board(board)
                .member(member)
                .build();

        commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, MemberDTO loginMember) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (loginMember.getRole() == Role.ADMIN || comment.getMember().getId().equals(loginMember.getId())) {
            commentRepository.delete(comment);
        } else {
            throw new IllegalStateException("본인의 댓글만 삭제할 수 있습니다.");
        }
    }

    public Long getBoardIdByCommentId(Long commentId) {
        return commentRepository.findById(commentId)
                .map(c -> c.getBoard().getId())
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
    }

    public List<CommentDTO> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId)
                .stream()
                .map(CommentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAllByBoardId(Long boardId) {
        commentRepository.deleteByBoardId(boardId);
    }
}