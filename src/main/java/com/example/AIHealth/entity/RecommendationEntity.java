package com.example.AIHealth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recommendation_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecommendationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String exerciseRecommendation;

    @Lob
    private String dietRecommendation;

    // ▼▼▼ [수정] int -> Integer 로 변경 (null 허용) ▼▼▼
    @Column
    private Integer breakfastCalories;

    @Column
    private Integer lunchCalories;

    @Column
    private Integer dinnerCalories;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;
}