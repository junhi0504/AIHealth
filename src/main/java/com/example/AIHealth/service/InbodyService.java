package com.example.AIHealth.service;

import com.example.AIHealth.dto.InbodyRequest;
import com.example.AIHealth.dto.InbodyResponse;
import com.example.AIHealth.entity.InbodyEntity;
import com.example.AIHealth.entity.MemberEntity;
import com.example.AIHealth.repository.InbodyRepository;
import com.example.AIHealth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InbodyService {

    private final InbodyRepository inbodyRepository;
    private final MemberRepository memberRepository;

    /** 특정 회원의 모든 인바디 기록 조회 (날짜 기준 정렬) */
    public List<InbodyResponse> getInbodyRecords(Long memberId) {
        List<InbodyEntity> entities = inbodyRepository.findByMember_IdOrderByMeasurementDateAsc(memberId);

        return entities.stream().map(entity -> {
            InbodyResponse dto = new InbodyResponse();
            dto.setHeight(entity.getHeight());  // ✨ 추가
            dto.setWeight(entity.getWeight());
            dto.setMuscleMass(entity.getMuscleMass());
            dto.setBodyFatPercentage(entity.getBodyFatPercentage());
            dto.setVisceralFatLevel(entity.getVisceralFatLevel()); // ✨ 추가

            if (entity.getMeasurementDate() != null) {
                dto.setMeasurementDate(entity.getMeasurementDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            dto.setGoal(entity.getGoal()); // 필요하면 목표도 포함
            return dto;
        }).collect(Collectors.toList());

    }

    /** 최신 인바디 데이터 1개 조회 (measurementDate 기준) */
    public Optional<InbodyEntity> findLatestByMemberId(Long memberId) {
        return inbodyRepository.findTopByMember_IdOrderByMeasurementDateDesc(memberId);
    }

    /** 회원 + 날짜 기준 데이터 조회 */
    public Optional<InbodyEntity> findByMemberIdAndDate(Long memberId, LocalDate date) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        return inbodyRepository.findByMemberAndMeasurementDate(member, date);
    }

    /** 저장/수정 */
    public void saveOrUpdateInbodyWithDate(Long memberId, InbodyRequest request, LocalDate date) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 날짜 기준 기존 데이터가 있으면 업데이트, 없으면 새로 생성
        InbodyEntity inbody = inbodyRepository.findByMemberAndMeasurementDate(member, date)
                .orElse(new InbodyEntity());

        inbody.setMember(member);
        inbody.setMeasurementDate(date);
        inbody.setHeight(request.getHeight());
        inbody.setWeight(request.getWeight());
        inbody.setMuscleMass(request.getMuscleMass());
        inbody.setBodyFatPercentage(request.getBodyFatPercentage());
        inbody.setVisceralFatLevel(request.getVisceralFatLevel());
        inbody.setGoal(request.getGoal());

        inbodyRepository.save(inbody);
    }
}
