package com.example.AIHealth.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    private Long id;
    private String content;
    private String memberName;
    private Long memberId;
    private LocalDateTime createdAt;
    private Long boardId;
}
