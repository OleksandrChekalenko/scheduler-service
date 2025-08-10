package com.treatment.schedulerservice.helper;

import com.treatment.schedulerservice.domain.RecurrenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurrencePattern {

    private RecurrenceType type;
    private List<LocalTime> times;
    private DayOfWeek dayOfWeek;
    private Integer dayOfMonth;

    public static RecurrencePattern daily(List<LocalTime> times) {
        return RecurrencePattern.builder()
                .type(RecurrenceType.DAILY)
                .times(times)
                .build();
    }

    public static RecurrencePattern weekly(DayOfWeek dayOfWeek, List<LocalTime> times) {
        return RecurrencePattern.builder()
                .type(RecurrenceType.WEEKLY)
                .dayOfWeek(dayOfWeek)
                .times(times)
                .build();
    }

    public static RecurrencePattern monthly(Integer dayOfMonth, List<LocalTime> times) {
        return RecurrencePattern.builder()
                .type(RecurrenceType.MONTHLY)
                .dayOfMonth(dayOfMonth)
                .times(times)
                .build();
    }

    public static RecurrencePattern once(List<LocalTime> times) {
        return RecurrencePattern.builder()
                .type(RecurrenceType.ONCE)
                .times(times)
                .build();
    }
}
