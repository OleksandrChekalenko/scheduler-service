package com.treatment.schedulerservice.repository;

import com.treatment.schedulerservice.entity.TreatmentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface TreatmentTaskRepository extends JpaRepository<TreatmentTask, Long> {

    @Query("SELECT tt FROM TreatmentTask tt WHERE tt.treatmentPlanId = :planId " +
           "AND tt.startTime = :startTime")
    Optional<TreatmentTask> findByTreatmentPlanIdAndStartTime(
            @Param("planId") Long planId,
            @Param("startTime") LocalDateTime startTime);
}
