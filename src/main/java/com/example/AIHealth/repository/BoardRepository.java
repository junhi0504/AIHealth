package com.example.AIHealth.repository;

import com.example.AIHealth.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    List<BoardEntity> findByTitleContainingIgnoreCase(String keyword);
    Page<BoardEntity> findByTitleContaining(String keyword, Pageable pageable);

    @Query("SELECT b FROM BoardEntity b JOIN FETCH b.member")
    List<BoardEntity> findAllWithMember();
}


