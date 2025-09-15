package com.example.AIHealth.service;

import com.example.AIHealth.dto.WorkoutDTO;
import com.example.AIHealth.entity.MemberEntity;
import com.example.AIHealth.entity.WorkoutEntity;
import com.example.AIHealth.repository.MemberRepository;
import com.example.AIHealth.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final MemberRepository memberRepository;

    // ... 기존 메소드들은 그대로 유지 ...
    @Transactional
    public void saveWorkoutsFromRecommendation(String exerciseText, Long memberId) {
        MemberEntity member = getMemberOrThrow(memberId);
        LocalDate today = LocalDate.now();
        if (workoutRepository.existsByMemberAndWorkoutDate(member, today)) {
            List<WorkoutEntity> existingWorkouts = workoutRepository.findByMemberAndWorkoutDate(member, today);
            workoutRepository.deleteAll(existingWorkouts);
        }
        List<String> todayExercises = extractTodayExerciseList(exerciseText);
        for (String exerciseLine : todayExercises) {
            if (exerciseLine.trim().isEmpty()) {
                continue;
            }
            WorkoutDTO dto = parseWorkoutDetails(exerciseLine, memberId, today);
            if (dto != null) {
                saveWorkout(dto);
            }
        }
    }

    private List<String> extractTodayExerciseList(String fullText) {
        if (fullText == null || fullText.isBlank()) {
            return new ArrayList<>();
        }
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String todayKor = switch (today) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            case SATURDAY -> "토요일";
            case SUNDAY -> "일요일";
        };
        String nextDayPattern = "(월요일|화요일|수요일|목요일|금요일|토요일|일요일)";
        Pattern pattern = Pattern.compile(todayKor + "\\(.*?\\)?" + "[:：]?" + "(.*?)" + "(?=" + nextDayPattern + "|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fullText);
        if (matcher.find()) {
            String todaySection = matcher.group(1).trim();
            String[] exercises = todaySection.split("[\\n]");
            List<String> result = new ArrayList<>();
            for (String exercise : exercises) {
                String trimmed = exercise.trim();
                if (!trimmed.isEmpty() && trimmed.matches("^\\d+\\..*")) {
                    result.add(trimmed);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    private WorkoutDTO parseWorkoutDetails(String exerciseLine, Long memberId, LocalDate date) {
        Pattern pattern = Pattern.compile(".*?-\\s*(\\d+)\\s*세트\\s*x\\s*(\\d+)\\s*회.*");
        Matcher matcher = pattern.matcher(exerciseLine);
        if (matcher.find()) {
            String name = cleanExerciseName(exerciseLine.split("-")[0]);
            int sets = Integer.parseInt(matcher.group(1));
            int reps = Integer.parseInt(matcher.group(2));
            return WorkoutDTO.builder().memberId(memberId).exerciseName(name).sets(sets).reps(reps).completed(false).workoutDate(date).build();
        }
        return null;
    }

    public List<WorkoutDTO> getWorkoutByDateForMember(Long memberId, LocalDate date) {
        MemberEntity member = getMemberOrThrow(memberId);
        return workoutRepository.findByMemberAndWorkoutDate(member, date).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void updateWorkoutCompletion(Long memberId, List<WorkoutDTO> completedWorkouts) {
        MemberEntity member = getMemberOrThrow(memberId);
        for (WorkoutDTO dto : completedWorkouts) {
            workoutRepository.findById(dto.getId()).ifPresent(entity -> {
                if (entity.getMember().getId().equals(member.getId())) {
                    entity.setCompleted(dto.isCompleted());
                    workoutRepository.save(entity);
                }
            });
        }
    }

    // ✨ [추가] 단일 운동 완료 상태 업데이트 메소드
    @Transactional
    public void updateSingleWorkoutCompletion(Long memberId, Long workoutId, boolean isCompleted) {
        MemberEntity member = getMemberOrThrow(memberId);
        WorkoutEntity workoutEntity = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운동 기록입니다."));

        // 본인의 운동 기록이 맞는지 확인
        if (!workoutEntity.getMember().getId().equals(member.getId())) {
            throw new SecurityException("자신의 운동 기록만 수정할 수 있습니다.");
        }

        workoutEntity.setCompleted(isCompleted);
        workoutRepository.save(workoutEntity);
    }

    @Transactional
    public void saveWorkout(WorkoutDTO dto) {
        MemberEntity member = getMemberOrThrow(dto.getMemberId());
        WorkoutEntity entity = WorkoutEntity.builder().member(member).exerciseName(dto.getExerciseName()).sets(dto.getSets()).reps(dto.getReps()).completed(dto.isCompleted()).workoutDate(dto.getWorkoutDate()).build();
        workoutRepository.save(entity);
    }

    @Transactional
    public void deleteWorkoutByIdAndMember(Long workoutId, Long memberId) {
        MemberEntity member = getMemberOrThrow(memberId);
        workoutRepository.findById(workoutId).ifPresent(workout -> {
            if (workout.getMember().getId().equals(member.getId())) {
                workoutRepository.delete(workout);
            } else {
                throw new IllegalStateException("삭제 권한이 없습니다.");
            }
        });
    }

    private MemberEntity getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new IllegalStateException("회원 정보가 없습니다."));
    }

    private WorkoutDTO toDTO(WorkoutEntity entity) {
        return WorkoutDTO.builder().id(entity.getId()).exerciseName(entity.getExerciseName()).sets(entity.getSets()).reps(entity.getReps()).completed(entity.isCompleted()).workoutDate(entity.getWorkoutDate()).memberId(entity.getMember().getId()).build();
    }

    private String cleanExerciseName(String raw) {
        return raw.replaceAll("^\\s*\\d+\\s*[.)]?\\s*", "").trim();
    }
}