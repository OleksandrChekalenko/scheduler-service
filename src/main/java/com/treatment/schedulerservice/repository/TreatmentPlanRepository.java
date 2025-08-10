package com.treatment.schedulerservice.repository;

import com.treatment.schedulerservice.entity.TreatmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Long> {

    @Query("SELECT tp FROM TreatmentPlan tp WHERE tp.active = true " +
           "AND tp.startTime <= :currentTime " +
           "AND (tp.endTime IS NULL OR tp.endTime > :currentTime)")
    List<TreatmentPlan> findActiveValidPlans(LocalDateTime currentTime);
}
