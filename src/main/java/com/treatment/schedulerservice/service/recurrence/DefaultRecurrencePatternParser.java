package com.treatment.schedulerservice.service.recurrence;

import com.treatment.schedulerservice.domain.RecurrenceType;
import com.treatment.schedulerservice.helper.RecurrencePattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class DefaultRecurrencePatternParser implements RecurrencePatternParser {

    @Override
    public RecurrencePattern parse(String patternString) {
        if (patternString == null || patternString.trim().isEmpty()) {
            throw new IllegalArgumentException("Recurrence pattern cannot be null or empty");
        }

        String[] parts = patternString.trim().split(":");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid recurrence pattern format: " + patternString);
        }

        try {
            RecurrenceType type = RecurrenceType.valueOf(parts[0].toUpperCase());

            return switch (type) {
                case DAILY -> parseDaily(parts);
                case WEEKLY -> parseWeekly(parts);
                case MONTHLY -> parseMonthly(parts);
                case ONCE -> parseOnce(parts);
            };
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse recurrence pattern: {}", patternString, e);
            throw new IllegalArgumentException("Invalid recurrence pattern: " + patternString, e);
        }
    }

    private RecurrencePattern parseDaily(String[] parts) {
        if (parts.length < 3) {
            throw new IllegalArgumentException("Daily pattern requires at least time specification");
        }
        List<LocalTime> times = parseTimesFromParts(parts, 1);
        return RecurrencePattern.daily(times);
    }

    private RecurrencePattern parseWeekly(String[] parts) {
        if (parts.length < 4) {
            throw new IllegalArgumentException("Weekly pattern requires day and time specification");
        }
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(parts[1].toUpperCase());
        List<LocalTime> times = parseTimesFromParts(parts, 2);
        return RecurrencePattern.weekly(dayOfWeek, times);
    }

    private RecurrencePattern parseMonthly(String[] parts) {
        if (parts.length < 4) {
            throw new IllegalArgumentException("Monthly pattern requires day and time specification");
        }
        int dayOfMonth = Integer.parseInt(parts[1]);
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("Day of month must be between 1 and 31");
        }
        List<LocalTime> times = parseTimesFromParts(parts, 2);
        return RecurrencePattern.monthly(dayOfMonth, times);
    }

    private RecurrencePattern parseOnce(String[] parts) {
        if (parts.length < 3) {
            throw new IllegalArgumentException("Once pattern requires time specification");
        }
        List<LocalTime> times = parseTimesFromParts(parts, 1);
        return RecurrencePattern.once(times);
    }

    private List<LocalTime> parseTimesFromParts(String[] parts, int startIndex) {
        List<LocalTime> times = new ArrayList<>();
        if (startIndex < parts.length) {
            String timesPart = String.join(":", Arrays.copyOfRange(parts, startIndex, parts.length));
            String[] timeStrings = timesPart.split(",");
            for (String timeString : timeStrings) {
                times.add(LocalTime.parse(timeString.trim()));
            }
        }
        if (times.isEmpty()) {
            throw new IllegalArgumentException("At least one time must be specified");
        }
        return times;
    }
}
