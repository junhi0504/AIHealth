package com.example.AIHealth.repository;

import com.example.AIHealth.entity.InbodyEntity;
import com.example.AIHealth.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InbodyRepository extends JpaRepository<InbodyEntity, Long> {

    Optional<InbodyEntity> findByMemberAndMeasurementDate(MemberEntity member, LocalDate measurementDate);

    Optional<InbodyEntity> findTopByMemberOrderByMeasurementDateDesc(MemberEntity member);

    List<InbodyEntity> findAllByMemberOrderByMeasurementDateDesc(MemberEntity member);

    // Member의 Id로 InbodyEntity 리스트를 찾고, 측정 날짜 오름차순으로 정렬
    List<InbodyEntity> findByMember_IdOrderByMeasurementDateAsc(Long memberId);

    // Member의 Id로 InbodyEntity를 찾고, ID 내림차순으로 정렬해서 가장 첫번째(최신) 데이터 1개를 가져옴
    Optional<InbodyEntity> findTopByMember_IdOrderByIdDesc(Long memberId);
}