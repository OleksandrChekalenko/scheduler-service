package com.treatment.schedulerservice.controller;

import com.treatment.schedulerservice.service.TreatmentSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final TreatmentSchedulerService schedulerService;

    @PostMapping("/run")
    public String runScheduler() {
        int tasksGenerated = schedulerService.generateTasksNow();
        return String.format("Scheduler executed. Generated %d tasks.", tasksGenerated);
    }
}
