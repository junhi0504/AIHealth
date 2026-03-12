package com.example.AIHealth.repository;

import com.example.AIHealth.entity.InbodyEntity;
import com.example.AIHealth.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InbodyRepository extends JpaRepository<InbodyEntity, Long> {

    // 특정 회원 + 특정 날짜 조회
    Optional<InbodyEntity> findByMemberAndMeasurementDate(MemberEntity member, LocalDate measurementDate);

    // 회원 기준, measurementDate 기준 최신 1개 조회
    Optional<InbodyEntity> findTopByMemberOrderByMeasurementDateDesc(MemberEntity member);

    // 회원 ID 기준, measurementDate 기준 최신 1개 조회
    Optional<InbodyEntity> findTopByMember_IdOrderByMeasurementDateDesc(Long memberId);

    // 회원 기준, measurementDate 기준 전체 리스트 조회 (최신 순)
    List<InbodyEntity> findAllByMemberOrderByMeasurementDateDesc(MemberEntity member);

    // 회원 ID 기준, measurementDate 기준 전체 리스트 조회 (오름차순)
    List<InbodyEntity> findByMember_IdOrderByMeasurementDateAsc(Long memberId);
}
