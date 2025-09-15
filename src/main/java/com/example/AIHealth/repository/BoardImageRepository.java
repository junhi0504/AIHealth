package com.example.AIHealth.repository;

import com.example.AIHealth.entity.BoardImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BoardImageRepository extends JpaRepository<BoardImageEntity, Long> {
    // 게시글 ID로 이미지 리스트 조회
    List<BoardImageEntity> findByBoardId(Long boardId);
}
