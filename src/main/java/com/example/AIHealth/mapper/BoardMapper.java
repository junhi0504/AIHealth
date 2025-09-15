package com.example.AIHealth.mapper;

import com.example.AIHealth.dto.BoardDTO;
import com.example.AIHealth.entity.BoardEntity;

public class BoardMapper {

    public static BoardDTO toDTO(BoardEntity entity) {
        if (entity == null) return null;

        BoardDTO dto = new BoardDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setViewCount(entity.getViewCount());

        if (entity.getMember() != null) {
            dto.setMemberId(entity.getMember().getId());
            dto.setMemberName(entity.getMember().getMemberName());
        }

        return dto;
    }
}
