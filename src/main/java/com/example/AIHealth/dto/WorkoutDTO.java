package com.example.AIHealth.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutDTO {
    private Long id;
    private Integer sets;
    private Integer reps;
    private String exerciseName;
    private boolean completed;
    private LocalDate workoutDate;
    private Long memberId;


}
