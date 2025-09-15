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
    private final MemberRepository memberRepository; // Member 조회를 위해 추가

    public List<InbodyResponse> getInbodyRecords(Long memberId) {
        // Repository에 새로 추가한, 정렬 기능이 포함된 메소드를 호출하도록 수정
        List<InbodyEntity> entities = inbodyRepository.findByMember_IdOrderByMeasurementDateAsc(memberId);

        return entities.stream().map(entity -> {
            InbodyResponse dto = new InbodyResponse();
            dto.setWeight(entity.getWeight());
            dto.setMuscleMass(entity.getMuscleMass());
            dto.setBodyFatPercentage(entity.getBodyFatPercentage());

            if (entity.getMeasurementDate() != null) {
                dto.setMeasurementDate(entity.getMeasurementDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // Controller가 찾지 못했던 메소드 추가
    public Optional<InbodyEntity> findLatestByMemberId(Long memberId) {
        return inbodyRepository.findTopByMember_IdOrderByIdDesc(memberId);
    }

    // Controller에서 사용하던 다른 메소드들도 여기에 포함되어야 합니다. (예시)
    public void saveOrUpdateInbodyWithDate(Long memberId, InbodyRequest request, LocalDate date) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

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

    public Optional<InbodyEntity> findByMemberIdAndDate(Long memberId, LocalDate date) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        return inbodyRepository.findByMemberAndMeasurementDate(member, date);
    }

    // ... (프로젝트의 다른 InbodyService 로직)
}