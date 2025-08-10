package com.treatment.schedulerservice.entity;

import com.treatment.schedulerservice.domain.TaskStatus;
import com.treatment.schedulerservice.domain.TreatmentAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "treatment_tasks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_plan_start_time", columnNames = {"treatment_plan_id", "start_time"})
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "treatment_action", nullable = false)
    private TreatmentAction treatmentAction;

    @NotBlank
    @Column(name = "subject_patient", nullable = false)
    private String subjectPatient;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.ACTIVE;

    @NotNull
    @Column(name = "treatment_plan_id", nullable = false)
    private Long treatmentPlanId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return TaskStatus.ACTIVE.equals(status);
    }
}
