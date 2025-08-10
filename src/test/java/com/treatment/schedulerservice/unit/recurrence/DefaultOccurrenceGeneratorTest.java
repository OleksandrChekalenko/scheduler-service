package com.treatment.schedulerservice.unit.recurrence;

import com.treatment.schedulerservice.helper.RecurrencePattern;
import com.treatment.schedulerservice.service.recurrence.DefaultOccurrenceGenerator;
import com.treatment.schedulerservice.service.recurrence.OccurrenceGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultOccurrenceGeneratorTest {

    private final OccurrenceGenerator generator = new DefaultOccurrenceGenerator();

    @Test
    void generate_daily_returnsOccurrencesInWindow() {
        RecurrencePattern pattern = RecurrencePattern.daily(List.of(LocalTime.of(8, 0), LocalTime.of(16, 0)));
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 3, 0, 0);
        List<LocalDateTime> occ = generator.generate(pattern, from, to, null, null);
        assertEquals(4, occ.size());
        assertTrue(occ.contains(LocalDateTime.of(2024, 1, 1, 8, 0)));
        assertTrue(occ.contains(LocalDateTime.of(2024, 1, 2, 16, 0)));
    }

    @ParameterizedTest
    @CsvSource({
            "2024-01-01T00:00:00,2024-01-15T00:00:00,MONDAY,10:00,2,2024-01-01T10:00:00,2024-01-08T10:00:00",
            "2024-02-01T00:00:00,2024-02-22T00:00:00,THURSDAY,07:30,3,2024-02-01T07:30:00,2024-02-08T07:30:00"
    })
    void generate_weekly_parametrized_countsAndContains(
            String fromStr,
            String toStr,
            String dayOfWeekStr,
            String timeStr,
            int expectedCount,
            String expectedFirst,
            String expectedSecond
    ) {
        DayOfWeek day = DayOfWeek.valueOf(dayOfWeekStr);
        LocalTime time = LocalTime.parse(timeStr);
        RecurrencePattern pattern = RecurrencePattern.weekly(day, List.of(time));

        LocalDateTime from = LocalDateTime.parse(fromStr);
        LocalDateTime to = LocalDateTime.parse(toStr);

        List<LocalDateTime> occ = generator.generate(pattern, from, to, null, null);
        assertEquals(expectedCount, occ.size());
        assertTrue(occ.contains(LocalDateTime.parse(expectedFirst)));
        assertTrue(occ.contains(LocalDateTime.parse(expectedSecond)));
    }

    @ParameterizedTest
    @CsvSource({"2023,28", "2024,29"})
    void generate_monthly_parametrized_handlesFebClamping(int year, int febExpectedDay) {
        RecurrencePattern pattern = RecurrencePattern.monthly(31, List.of(LocalTime.of(9, 0)));
        LocalDateTime from = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(year, 4, 1, 0, 0);

        List<LocalDateTime> occ = generator.generate(pattern, from, to, null, null);

        assertTrue(occ.contains(LocalDateTime.of(year, 1, 31, 9, 0)));
        assertTrue(occ.contains(LocalDateTime.of(year, 2, febExpectedDay, 9, 0)));
        assertTrue(occ.contains(LocalDateTime.of(year, 3, 31, 9, 0)));
        assertEquals(3, occ.size());
    }

    @Test
    void generate_weekly_overTwoWeeks_returnsTwoMondays() {
        RecurrencePattern pattern = RecurrencePattern.weekly(DayOfWeek.MONDAY, List.of(LocalTime.of(10, 0)));
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 15, 0, 0);
        List<LocalDateTime> occ = generator.generate(pattern, from, to, null, null);
        assertEquals(2, occ.size());
        assertTrue(occ.contains(LocalDateTime.of(2024, 1, 1, 10, 0)));
        assertTrue(occ.contains(LocalDateTime.of(2024, 1, 8, 10, 0)));
    }

    @Test
    void generate_monthly_clampsShortMonths() {
        RecurrencePattern pattern = RecurrencePattern.monthly(31, List.of(LocalTime.of(9, 0)));
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 4, 1, 0, 0);
        List<LocalDateTime> occ = generator.generate(pattern, from, to, null, null);
        assertTrue(occ.contains(LocalDateTime.of(2024, 2, 29, 9, 0)));
    }

    @Test
    void generate_once_usesPlanStartDate() {
        RecurrencePattern pattern = RecurrencePattern.once(List.of(LocalTime.of(10, 30)));
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 2, 0, 0);
        LocalDateTime planStart = LocalDateTime.of(2024, 1, 1, 8, 0);
        List<LocalDateTime> occ = generator.generate(pattern, from, to, planStart, null);
        assertEquals(1, occ.size());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 30), occ.getFirst());
    }

    @Test
    void generate_endTimeInclusive() {
        RecurrencePattern pattern = RecurrencePattern.daily(List.of(LocalTime.of(8, 0)));
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 5, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 3, 0, 0);
        List<LocalDateTime> occ = generator.generate(pattern, from, to, null, end);
        assertFalse(occ.contains(LocalDateTime.of(2024, 1, 3, 8, 0)));
        assertTrue(occ.contains(LocalDateTime.of(2024, 1, 2, 8, 0)));
    }
}
