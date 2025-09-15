package com.example.AIHealth.mapper;

import com.example.AIHealth.dto.CommentDTO;
import com.example.AIHealth.entity.CommentEntity;

public class CommentMapper {

    public static CommentDTO toDTO(CommentEntity entity) {
        if (entity == null) return null;

        return CommentDTO.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .memberId(entity.getMember().getId())
                .memberName(entity.getMember().getMemberName())
                .boardId(entity.getBoard().getId())
                .build();
    }
}
