package com.treatment.schedulerservice.service.recurrence;

import com.treatment.schedulerservice.domain.RecurrenceType;
import com.treatment.schedulerservice.helper.RecurrencePattern;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultOccurrenceGenerator implements OccurrenceGenerator {

    @Override
    public List<LocalDateTime> generate(RecurrencePattern pattern,
                                        LocalDateTime fromTime,
                                        LocalDateTime toTime,
                                        LocalDateTime planStartTime,
                                        LocalDateTime planEndTime) {
        List<LocalDateTime> occurrences = new ArrayList<>();

        if (pattern.getType() == RecurrenceType.ONCE) {
            LocalDateTime onceDateTime = planStartTime.toLocalDate().atTime(pattern.getTimes().getFirst());
            if (isWithinWindow(onceDateTime, fromTime, toTime, planEndTime)) {
                occurrences.add(onceDateTime);
            }
            return occurrences;
        }

        LocalDateTime current = fromTime;
        if (pattern.getType() == RecurrenceType.MONTHLY) {
            current = adjustToValidMonthlyDate(current, pattern.getDayOfMonth());
        }

        int maxIterations = 1000;
        int iterations = 0;

        while (current.isBefore(toTime) && iterations < maxIterations) {
            iterations++;

            List<LocalDateTime> dailyOccurrences = calculateOccurrencesForDate(pattern, current);
            for (LocalDateTime occurrence : dailyOccurrences) {
                if (isWithinWindow(occurrence, fromTime, toTime, planEndTime)) {
                    occurrences.add(occurrence);
                }
            }

            current = getNextCalculationDate(pattern, current);
        }

        return occurrences;
    }

    private List<LocalDateTime> calculateOccurrencesForDate(RecurrencePattern pattern, LocalDateTime date) {
        List<LocalDateTime> occurrences = new ArrayList<>();

        boolean shouldGenerate = switch (pattern.getType()) {
            case DAILY -> true;
            case WEEKLY -> date.getDayOfWeek() == pattern.getDayOfWeek();
            case MONTHLY -> {
                int desired = pattern.getDayOfMonth();
                int lastDay = date.toLocalDate().lengthOfMonth();
                int effectiveDay = Math.min(Math.max(desired, 1), lastDay);
                yield date.getDayOfMonth() == effectiveDay;
            }
            case ONCE -> false;
        };

        if (shouldGenerate) {
            occurrences = pattern.getTimes().stream()
                    .map(time -> date.toLocalDate().atTime(time))
                    .collect(Collectors.toList());
        }

        return occurrences;
    }

    private LocalDateTime getNextCalculationDate(RecurrencePattern pattern, LocalDateTime current) {
        return switch (pattern.getType()) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.with(TemporalAdjusters.next(pattern.getDayOfWeek()));
            case MONTHLY -> adjustToValidMonthlyDate(current.plusMonths(1), pattern.getDayOfMonth());
            case ONCE -> current.plusYears(1);
        };
    }

    private LocalDateTime adjustToValidMonthlyDate(LocalDateTime base, int desiredDayOfMonth) {
        int lastDay = base.toLocalDate().lengthOfMonth();
        int day = Math.min(Math.max(desiredDayOfMonth, 1), lastDay);
        return base.withDayOfMonth(day);
    }

    private boolean isWithinWindow(LocalDateTime occurrence,
                                   LocalDateTime fromTime,
                                   LocalDateTime toTime,
                                   LocalDateTime planEndTime) {
        if (occurrence.isBefore(fromTime) || occurrence.isAfter(toTime) || occurrence.isEqual(toTime)) {
            return false;
        }
        return planEndTime == null || !occurrence.isAfter(planEndTime);
    }
}
