package com.treatment.schedulerservice.config;

import com.treatment.schedulerservice.domain.TreatmentAction;
import com.treatment.schedulerservice.entity.TreatmentPlan;
import com.treatment.schedulerservice.repository.TreatmentPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TreatmentPlanRepository treatmentPlanRepository;

    @Override
    public void run(String... args) {
        if (treatmentPlanRepository.count() == 0) {
            log.info("Creating test treatment plans...");
            createTestPlans();
            log.info("Test data created");
        }
    }

    private void createTestPlans() {
        LocalDateTime now = LocalDateTime.now();

        List<TreatmentPlan> testPlans = List.of(
            // Daily plan - twice a day
            TreatmentPlan.builder()
                .treatmentAction(TreatmentAction.ACTION_A)
                .subjectPatient("PATIENT_001")
                .startTime(now.minusHours(1))
                .endTime(now.plusDays(7))
                .recurrencePattern("DAILY:08:00,20:00")
                .active(true)
                .build(),

            // Weekly plan - once a week
            TreatmentPlan.builder()
                .treatmentAction(TreatmentAction.ACTION_B)
                .subjectPatient("PATIENT_002")
                .startTime(now.minusHours(1))
                .recurrencePattern("WEEKLY:MONDAY:10:00")
                .active(true)
                .build()
        );

        treatmentPlanRepository.saveAll(testPlans);
        log.info("Created {} test treatment plans", testPlans.size());
    }
} 