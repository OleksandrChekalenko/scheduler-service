package com.treatment.schedulerservice.service;

import com.treatment.schedulerservice.domain.TaskStatus;
import com.treatment.schedulerservice.entity.TreatmentPlan;
import com.treatment.schedulerservice.entity.TreatmentTask;
import com.treatment.schedulerservice.helper.RecurrencePattern;
import com.treatment.schedulerservice.repository.TreatmentPlanRepository;
import com.treatment.schedulerservice.repository.TreatmentTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentSchedulerService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TreatmentTaskRepository treatmentTaskRepository;
    private final RecurrencePatternService recurrencePatternService;

    @Value("${scheduler.look-ahead-hours:1}")
    private int lookAheadHours = 1;

    @Scheduled(fixedRateString = "${scheduler.execution-interval-ms:300000}")
    @Transactional
    public void generateScheduledTasks() {
        log.info("Starting scheduled task generation");
        generateTasksNow();
    }

    @Transactional
    public int generateTasksNow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookAheadTime = now.plusHours(lookAheadHours);
        
        List<TreatmentPlan> activePlans = treatmentPlanRepository.findActiveValidPlans(now);
        log.info("Found {} active treatment plans", activePlans.size());
        
        int totalTasksGenerated = 0;
        for (TreatmentPlan plan : activePlans) {
            totalTasksGenerated += generateTasksForPlan(plan, now, lookAheadTime);
        }
        
        log.info("Generated {} total tasks", totalTasksGenerated);
        return totalTasksGenerated;
    }

    private int generateTasksForPlan(TreatmentPlan plan, LocalDateTime fromTime, LocalDateTime toTime) {
        try {
            RecurrencePattern pattern = recurrencePatternService.parsePattern(plan.getRecurrencePattern());
            List<LocalDateTime> occurrences = recurrencePatternService.getNextOccurrences(
                    pattern, fromTime, toTime, plan.getStartTime(), plan.getEndTime());
            
            int tasksCreated = 0;
            for (LocalDateTime occurrence : occurrences) {
                if (treatmentTaskRepository.findByTreatmentPlanIdAndStartTime(plan.getId(), occurrence).isEmpty()) {
                    createTreatmentTask(plan, occurrence);
                    tasksCreated++;
                }
            }
            
            if (tasksCreated > 0) {
                log.info("Generated {} tasks for plan ID {} (patient: {})", 
                        tasksCreated, plan.getId(), plan.getSubjectPatient());
            }
            
            return tasksCreated;
        } catch (Exception e) {
            log.error("Failed to generate tasks for plan ID {}: {}", plan.getId(), e.getMessage());
            return 0;
        }
    }

    private void createTreatmentTask(TreatmentPlan plan, LocalDateTime startTime) {
        TreatmentTask task = TreatmentTask.builder()
                .treatmentAction(plan.getTreatmentAction())
                .subjectPatient(plan.getSubjectPatient())
                .startTime(startTime)
                .status(TaskStatus.ACTIVE)
                .treatmentPlanId(plan.getId())
                .build();

        try {
            treatmentTaskRepository.save(task);
            log.debug("Created task for patient {} at {}", plan.getSubjectPatient(), startTime);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.debug("Task already exists for plan {} at {} (unique constraint)", plan.getId(), startTime);
        }
    }
}
