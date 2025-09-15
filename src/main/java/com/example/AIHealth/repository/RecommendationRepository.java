package com.example.AIHealth.repository;

import com.example.AIHealth.entity.MemberEntity;
import com.example.AIHealth.entity.RecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {

    // 특정 회원의 가장 최근 추천 정보를 조회
    Optional<RecommendationEntity> findTopByMemberOrderByIdDesc(MemberEntity member);
}
