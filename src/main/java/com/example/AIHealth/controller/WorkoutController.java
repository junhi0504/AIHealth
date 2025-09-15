package com.example.AIHealth.controller;

import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.dto.WorkoutDTO;
import com.example.AIHealth.service.MemberService;
import com.example.AIHealth.service.WorkoutService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/workout")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final MemberService memberService;

    // 공통 로직: 로그인 멤버 정보를 Model에 추가하고 MemberDTO 객체를 반환
    private MemberDTO addLoginMemberAndGet(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            MemberDTO loginMember = memberService.findByMemberEmail(userEmail);
            model.addAttribute("loginMember", loginMember);
            return loginMember;
        }
        return null;
    }

    @GetMapping
    public String workoutPage(@RequestParam(value = "workoutDate", required = false) String workoutDateStr,
                              Authentication authentication,
                              Model model) {
        MemberDTO loginMember = addLoginMemberAndGet(authentication, model);
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        Long loginMemberId = loginMember.getId();

        LocalDate workoutDate;
        try {
            workoutDate = (workoutDateStr == null || workoutDateStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(workoutDateStr);
        } catch (DateTimeParseException e) {
            workoutDate = LocalDate.now();
        }
        List<WorkoutDTO> workouts = workoutService.getWorkoutByDateForMember(loginMemberId, workoutDate);
        List<WorkoutDTO> filteredWorkouts = workouts.stream().filter(w -> w.getExerciseName() != null && !w.getExerciseName().trim().isEmpty()).toList();
        model.addAttribute("todayWorkouts", filteredWorkouts);
        model.addAttribute("workoutDate", workoutDate.toString());
        WorkoutWrapperDTO wrapper = new WorkoutWrapperDTO();
        wrapper.setCompletedWorkouts(new ArrayList<>(filteredWorkouts));
        model.addAttribute("completedWorkoutsWrapper", wrapper);
        model.addAttribute("contentTemplate", "workout");
        model.addAttribute("pageTitle", "운동일지");
        return "layout";
    }

    @PostMapping("/save")
    public String saveWorkout(@ModelAttribute("completedWorkoutsWrapper") WorkoutWrapperDTO completedWorkoutsWrapper,
                              @RequestParam("workoutDate") String workoutDateStr,
                              Authentication authentication,
                              Model model) {
        MemberDTO loginMember = addLoginMemberAndGet(authentication, model);
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        Long loginMemberId = loginMember.getId();

        LocalDate workoutDate;
        try {
            workoutDate = LocalDate.parse(workoutDateStr);
        } catch (DateTimeParseException e) {
            workoutDate = LocalDate.now();
        }
        if (completedWorkoutsWrapper == null) {
            completedWorkoutsWrapper = new WorkoutWrapperDTO();
        }
        if (completedWorkoutsWrapper.getCompletedWorkouts() == null) {
            completedWorkoutsWrapper.setCompletedWorkouts(new ArrayList<>());
        }
        List<WorkoutDTO> validWorkouts = completedWorkoutsWrapper.getCompletedWorkouts().stream().filter(w -> w.getId() != null).toList();
        workoutService.updateWorkoutCompletion(loginMemberId, validWorkouts);
        List<WorkoutDTO> workouts = workoutService.getWorkoutByDateForMember(loginMemberId, workoutDate);
        List<WorkoutDTO> filteredWorkouts = workouts.stream().filter(w -> w.getExerciseName() != null && !w.getExerciseName().trim().isEmpty()).toList();
        model.addAttribute("todayWorkouts", filteredWorkouts);
        model.addAttribute("workoutDate", workoutDate.toString());
        WorkoutWrapperDTO wrapper = new WorkoutWrapperDTO();
        wrapper.setCompletedWorkouts(new ArrayList<>(filteredWorkouts));
        model.addAttribute("completedWorkoutsWrapper", wrapper);
        model.addAttribute("saveSuccess", true);
        model.addAttribute("contentTemplate", "workout");
        model.addAttribute("pageTitle", "운동일지");
        return "layout";
    }

    @GetMapping("/add")
    public String showAddWorkoutPage(@RequestParam(value = "workoutDate", required = false) String workoutDateStr,
                                     Authentication authentication,
                                     Model model) {
        if (authentication == null) {
            return "redirect:/member/login";
        }
        LocalDate workoutDate;
        try {
            workoutDate = (workoutDateStr == null || workoutDateStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(workoutDateStr);
        } catch (DateTimeParseException e) {
            workoutDate = LocalDate.now();
        }
        model.addAttribute("workoutDate", workoutDate.toString());
        return "addWorkout";
    }

    @PostMapping("/add")
    @ResponseBody
    public String addSelectedWorkouts(
            @RequestParam(value = "selectedExercises", required = false) List<String> selectedExercises,
            @RequestParam Map<String, String> allRequestParams,
            @RequestParam("workoutDate") String workoutDateStr,
            Authentication authentication) {
        MemberDTO member = memberService.findByMemberEmail(authentication.getName());
        Long loginMemberId = (member != null) ? member.getId() : null;

        if (loginMemberId == null) {
            return "로그인 필요";
        }
        LocalDate workoutDate;
        try {
            workoutDate = LocalDate.parse(workoutDateStr);
        } catch (DateTimeParseException e) {
            workoutDate = LocalDate.now();
        }
        if (selectedExercises == null || selectedExercises.isEmpty()) {
            return "운동 종목이 선택되지 않았습니다.";
        }
        for (String exerciseName : selectedExercises) {
            String setsStr = allRequestParams.get("sets_" + exerciseName.replace(" ", ""));
            String repsStr = allRequestParams.get("reps_" + exerciseName.replace(" ", ""));
            if (setsStr == null || repsStr == null || setsStr.isBlank() || repsStr.isBlank()) {
                return "세트 또는 횟수 값이 누락되었습니다: " + exerciseName;
            }
            try {
                int sets = Integer.parseInt(setsStr);
                int reps = Integer.parseInt(repsStr);
                if (sets < 1 || reps < 1) {
                    return "세트와 횟수는 1 이상이어야 합니다: " + exerciseName;
                }
                WorkoutDTO dto = new WorkoutDTO();
                dto.setExerciseName(exerciseName);
                dto.setSets(sets);
                dto.setReps(reps);
                dto.setWorkoutDate(workoutDate);
                dto.setMemberId(loginMemberId);
                dto.setCompleted(false);
                workoutService.saveWorkout(dto);
            } catch (NumberFormatException e) {
                return "세트 또는 횟수 값이 숫자가 아닙니다: " + exerciseName;
            } catch (Exception e) {
                return "저장 중 오류가 발생했습니다: " + e.getMessage();
            }
        }
        return "success";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteWorkout(@PathVariable Long id, Authentication authentication) {
        MemberDTO member = memberService.findByMemberEmail(authentication.getName());
        Long loginMemberId = (member != null) ? member.getId() : null;
        if (loginMemberId == null) {
            throw new IllegalStateException("로그인 필요");
        }
        workoutService.deleteWorkoutByIdAndMember(id, loginMemberId);
        return "success";
    }

    @Getter @Setter
    public static class WorkoutStatusUpdateRequest {
        private Long workoutId;
        private boolean completed;
    }

    @PostMapping("/updateStatus")
    @ResponseBody
    public ResponseEntity<String> updateWorkoutStatus(@RequestBody WorkoutStatusUpdateRequest request, Authentication authentication) {
        MemberDTO member = memberService.findByMemberEmail(authentication.getName());
        Long loginMemberId = (member != null) ? member.getId() : null;
        if (loginMemberId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        try {
            workoutService.updateSingleWorkoutCompletion(loginMemberId, request.getWorkoutId(), request.isCompleted());
            return ResponseEntity.ok("상태 업데이트 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("업데이트 중 오류 발생: " + e.getMessage());
        }
    }

    @Getter
    @Setter
    public static class WorkoutWrapperDTO {
        private List<WorkoutDTO> completedWorkouts;
    }
}