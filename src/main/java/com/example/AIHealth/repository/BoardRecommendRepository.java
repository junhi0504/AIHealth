package com.example.AIHealth.repository;

import com.example.AIHealth.entity.BoardRecommendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRecommendRepository extends JpaRepository<BoardRecommendEntity, Long> {
    boolean existsByBoardIdAndMemberId(Long boardId, Long memberId);
    void deleteByBoardIdAndMemberId(Long boardId, Long memberId);
    int countByBoardId(Long boardId);

    // ✅ 게시글 삭제 시 추천 기록 전체 삭제
    void deleteAllByBoardId(Long boardId);
}

