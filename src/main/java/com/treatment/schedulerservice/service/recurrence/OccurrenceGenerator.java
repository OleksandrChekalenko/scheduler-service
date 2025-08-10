package com.treatment.schedulerservice.service.recurrence;

import com.treatment.schedulerservice.helper.RecurrencePattern;

import java.time.LocalDateTime;
import java.util.List;

public interface OccurrenceGenerator {
    List<LocalDateTime> generate(
            RecurrencePattern pattern,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime planStartTime,
            LocalDateTime planEndTime
    );
}
