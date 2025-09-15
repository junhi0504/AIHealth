package com.example.AIHealth.repository;

import com.example.AIHealth.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByBoardIdOrderByCreatedAtAsc(Long boardId);

    @Query("SELECT c.board.id, COUNT(c) FROM CommentEntity c WHERE c.board.id IN :boardIds GROUP BY c.board.id")
    List<Object[]> countCommentsByBoardIds(@Param("boardIds") List<Long> boardIds);

    @Transactional
    void deleteByBoardId(Long boardId);
}