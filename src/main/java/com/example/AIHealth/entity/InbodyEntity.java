package com.example.AIHealth.entity;

import com.example.AIHealth.dto.InbodyRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "inbody_table", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "measurement_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class InbodyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double height;
    private double weight;
    private double bodyFatPercentage;
    private double muscleMass;
    private int visceralFatLevel;

    private String goal; // 운동 목표

    @Column(name = "measurement_date", nullable = false)
    private LocalDate measurementDate;  // 측정 날짜 추가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    // 👇 서비스에서 사용될 팩토리 메서드 (날짜 필드 추가)
    public static InbodyEntity of(InbodyRequest dto, MemberEntity member, LocalDate measurementDate) {
        InbodyEntity entity = new InbodyEntity();
        entity.setMember(member);
        entity.setHeight(dto.getHeight());
        entity.setWeight(dto.getWeight());
        entity.setBodyFatPercentage(dto.getBodyFatPercentage());
        entity.setMuscleMass(dto.getMuscleMass());
        entity.setVisceralFatLevel(dto.getVisceralFatLevel());
        entity.setGoal(dto.getGoal());
        entity.setMeasurementDate(measurementDate); // 날짜 세팅
        return entity;
    }
}
