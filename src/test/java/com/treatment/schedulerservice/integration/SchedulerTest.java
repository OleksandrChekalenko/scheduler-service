package com.treatment.schedulerservice.integration;

import com.treatment.schedulerservice.domain.TreatmentAction;
import com.treatment.schedulerservice.entity.TreatmentPlan;
import com.treatment.schedulerservice.entity.TreatmentTask;
import com.treatment.schedulerservice.repository.TreatmentPlanRepository;
import com.treatment.schedulerservice.repository.TreatmentTaskRepository;
import com.treatment.schedulerservice.service.TreatmentSchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SchedulerTest {

    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private TreatmentTaskRepository treatmentTaskRepository;

    @Autowired
    private TreatmentSchedulerService schedulerService;

    @Test
    void schedulerGeneratesTaskForImminentOncePlan_expectedTaskPersisted() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime occurrenceTime = now.plusMinutes(1).withSecond(0).withNano(0).toLocalTime();
        String pattern = "ONCE:" + DateTimeFormatter.ofPattern("HH:mm").format(occurrenceTime);

        TreatmentPlan plan = TreatmentPlan.builder()
                .treatmentAction(TreatmentAction.ACTION_A)
                .subjectPatient("IT_PATIENT")
                .startTime(now.minusMinutes(5))
                .endTime(null)
                .recurrencePattern(pattern)
                .active(true)
                .build();

        plan = treatmentPlanRepository.save(plan);

        LocalDate occurrenceDate = plan.getStartTime().toLocalDate();
        LocalDateTime expectedTaskTime = occurrenceDate.atTime(occurrenceTime);

        int generated = schedulerService.generateTasksNow();

        Optional<TreatmentTask> maybeTask = treatmentTaskRepository
                .findByTreatmentPlanIdAndStartTime(plan.getId(), expectedTaskTime);

        assertTrue(generated >= 1, "Expected at least one task to be generated");
        assertTrue(maybeTask.isPresent(), "Expected a task for the plan at the expected time");
        TreatmentTask task = maybeTask.get();
        assertEquals(plan.getSubjectPatient(), task.getSubjectPatient());
        assertEquals(plan.getTreatmentAction(), task.getTreatmentAction());
        assertEquals(expectedTaskTime, task.getStartTime());
    }
}
