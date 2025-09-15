package com.example.AIHealth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "workout_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String exerciseName;        // 운동 이름

    private boolean completed;          // 수행 여부

    private LocalDate workoutDate;      // 날짜
    @Column(name = "sets")
    private Integer sets;

    @Column(name = "reps")
    private Integer reps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;        // 연관된 사용자
}
