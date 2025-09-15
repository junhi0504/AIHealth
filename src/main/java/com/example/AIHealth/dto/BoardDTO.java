package com.example.AIHealth.dto;

import com.example.AIHealth.entity.BoardEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class BoardDTO {
    private Long id;
    private String title;
    private String content;
    private Long memberId;
    private String memberName;
    private int viewCount;
    private int recommendCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> deleteImageIds;

    private Long commentCount = 0L;

    private List<BoardImageDTO> images;

    public BoardDTO() {}

    public BoardDTO(BoardEntity board) {
        if (board == null) throw new IllegalArgumentException("BoardEntity가 null입니다.");

        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        if (board.getMember() != null) {
            this.memberId = board.getMember().getId();
            this.memberName = board.getMember().getMemberName();
        } else {
            this.memberName = "(탈퇴한 회원)";
        }
        this.viewCount = board.getViewCount();
        this.recommendCount = board.getRecommendCount();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();

        if (board.getImages() != null) {
            this.images = board.getImages().stream()
                    .map(BoardImageDTO::new)
                    .collect(Collectors.toList());
        }
    }
}
