package com.treatment.schedulerservice.unit.service;

import com.treatment.schedulerservice.domain.TaskStatus;
import com.treatment.schedulerservice.domain.TreatmentAction;
import com.treatment.schedulerservice.entity.TreatmentPlan;
import com.treatment.schedulerservice.entity.TreatmentTask;
import com.treatment.schedulerservice.repository.TreatmentPlanRepository;
import com.treatment.schedulerservice.repository.TreatmentTaskRepository;
import com.treatment.schedulerservice.service.RecurrencePatternService;
import com.treatment.schedulerservice.service.TreatmentSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentSchedulerServiceTest {

    @Mock
    private TreatmentPlanRepository planRepository;

    @Mock
    private TreatmentTaskRepository taskRepository;

    @Mock
    private RecurrencePatternService recurrenceService;

    @InjectMocks
    private TreatmentSchedulerService schedulerService;

    private TreatmentPlan samplePlan;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(schedulerService, "lookAheadHours", 1);

        LocalDateTime now = LocalDateTime.now();
        samplePlan = TreatmentPlan.builder()
                .id(100L)
                .treatmentAction(TreatmentAction.ACTION_A)
                .subjectPatient("PATIENT_MOCK")
                .startTime(now.minusMinutes(10))
                .endTime(now.plusDays(1))
                .recurrencePattern("DAILY:10:00")
                .active(true)
                .build();
    }

    @Test
    void generatesTaskForOccurrenceWithinWindow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurrence = now.plusMinutes(30).withSecond(0).withNano(0);

        when(planRepository.findActiveValidPlans(any())).thenReturn(List.of(samplePlan));
        when(recurrenceService.getNextOccurrences(any(), any(), any(), any(), any())).thenReturn(List.of(occurrence));
        when(taskRepository.findByTreatmentPlanIdAndStartTime(samplePlan.getId(), occurrence))
                .thenReturn(Optional.empty());

        int generated = schedulerService.generateTasksNow();

        assertEquals(1, generated);
        ArgumentCaptor<TreatmentTask> captor = ArgumentCaptor.forClass(TreatmentTask.class);
        verify(taskRepository, times(1)).save(captor.capture());
        TreatmentTask saved = captor.getValue();
        assertEquals(samplePlan.getId(), saved.getTreatmentPlanId());
        assertEquals(occurrence, saved.getStartTime());
        assertEquals(TaskStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void doesNotCreateDuplicateIfTaskExists() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurrence = now.plusMinutes(20).withSecond(0).withNano(0);

        when(planRepository.findActiveValidPlans(any())).thenReturn(List.of(samplePlan));
        when(recurrenceService.getNextOccurrences(any(), any(), any(), any(), any())).thenReturn(List.of(occurrence));
        when(taskRepository.findByTreatmentPlanIdAndStartTime(samplePlan.getId(), occurrence))
                .thenReturn(Optional.of(TreatmentTask.builder().id(1L).build()));

        int generated = schedulerService.generateTasksNow();

        assertEquals(0, generated);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void handlesUniqueConstraintViolationGracefully() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurrence = now.plusMinutes(15).withSecond(0).withNano(0);

        when(planRepository.findActiveValidPlans(any())).thenReturn(List.of(samplePlan));
        when(recurrenceService.getNextOccurrences(any(), any(), any(), any(), any())).thenReturn(List.of(occurrence));
        when(taskRepository.findByTreatmentPlanIdAndStartTime(samplePlan.getId(), occurrence))
                .thenReturn(Optional.empty());
        when(taskRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        int generated = schedulerService.generateTasksNow();

        assertEquals(1, generated);
        verify(taskRepository, times(1)).save(any());
    }

    @Test
    void allowsOccurrenceExactlyAtEndTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusMinutes(40).withSecond(0).withNano(0);

        TreatmentPlan plan = TreatmentPlan.builder()
                .id(samplePlan.getId())
                .treatmentAction(samplePlan.getTreatmentAction())
                .subjectPatient(samplePlan.getSubjectPatient())
                .startTime(samplePlan.getStartTime())
                .endTime(end)
                .recurrencePattern(samplePlan.getRecurrencePattern())
                .active(true)
                .build();

        when(planRepository.findActiveValidPlans(any())).thenReturn(List.of(plan));
        when(recurrenceService.getNextOccurrences(any(), any(), any(), any(), any()))
                .thenReturn(List.of(end));
        when(taskRepository.findByTreatmentPlanIdAndStartTime(plan.getId(), end)).thenReturn(Optional.empty());

        int generated = schedulerService.generateTasksNow();
        assertEquals(1, generated);
        verify(taskRepository, times(1)).save(any());
    }

    @Test
    void returnsZeroWhenNoActivePlans() {
        when(planRepository.findActiveValidPlans(any())).thenReturn(List.of());

        int generated = schedulerService.generateTasksNow();

        assertEquals(0, generated);
        verifyNoInteractions(taskRepository);
    }
}
