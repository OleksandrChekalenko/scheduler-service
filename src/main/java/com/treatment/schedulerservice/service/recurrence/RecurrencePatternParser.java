package com.treatment.schedulerservice.service.recurrence;

import com.treatment.schedulerservice.helper.RecurrencePattern;

public interface RecurrencePatternParser {
    RecurrencePattern parse(String patternString);
}
