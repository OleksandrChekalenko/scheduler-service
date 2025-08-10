package com.treatment.schedulerservice.service;

import com.treatment.schedulerservice.helper.RecurrencePattern;
import com.treatment.schedulerservice.service.recurrence.OccurrenceGenerator;
import com.treatment.schedulerservice.service.recurrence.RecurrencePatternParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurrencePatternService {

    private final RecurrencePatternParser parser;
    private final OccurrenceGenerator generator;

    public RecurrencePattern parsePattern(String patternString) {
        return parser.parse(patternString);
    }

    public List<LocalDateTime> getNextOccurrences(RecurrencePattern pattern,
                                                  LocalDateTime fromTime,
                                                  LocalDateTime toTime,
                                                  LocalDateTime planStartTime,
                                                  LocalDateTime planEndTime) {
        return generator.generate(pattern, fromTime, toTime, planStartTime, planEndTime);
    }
}
