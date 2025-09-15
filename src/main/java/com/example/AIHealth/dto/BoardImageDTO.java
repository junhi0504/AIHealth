package com.example.AIHealth.dto;

import com.example.AIHealth.entity.BoardImageEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardImageDTO {
    private Long id;
    private String fileName;
    private String filePath;

    public BoardImageDTO() {}

    public BoardImageDTO(BoardImageEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.fileName = entity.getFileName();
            this.filePath = entity.getFilePath();
        }
    }
}
