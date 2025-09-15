package com.example.AIHealth.repository;

import com.example.AIHealth.entity.WorkoutEntity;
import com.example.AIHealth.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<WorkoutEntity, Long> {

    // 특정 회원의 특정 날짜 운동일지 조회
    List<WorkoutEntity> findByMemberAndWorkoutDate(MemberEntity member, LocalDate workoutDate);

    // 특정 회원의 운동일지를 날짜순으로 전체 조회
    List<WorkoutEntity> findByMemberOrderByWorkoutDateDesc(MemberEntity member);

    // 특정 기간 운동일지 조회 (캘린더용)
    List<WorkoutEntity> findByMemberAndWorkoutDateBetween(MemberEntity member, LocalDate startDate, LocalDate endDate);

    boolean existsByMemberAndWorkoutDate(MemberEntity member, LocalDate workoutDate);
}
