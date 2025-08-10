package com.treatment.schedulerservice.unit.recurrence;

import com.treatment.schedulerservice.domain.RecurrenceType;
import com.treatment.schedulerservice.helper.RecurrencePattern;
import com.treatment.schedulerservice.service.recurrence.DefaultRecurrencePatternParser;
import com.treatment.schedulerservice.service.recurrence.RecurrencePatternParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRecurrencePatternParserTest {

    private final RecurrencePatternParser parser = new DefaultRecurrencePatternParser();

    @Test
    void parse_daily_returnsTimes() {
        RecurrencePattern p = parser.parse("DAILY:08:00,16:00");
        assertEquals(RecurrenceType.DAILY, p.getType());
        assertTrue(p.getTimes().contains(LocalTime.of(8, 0)));
        assertTrue(p.getTimes().contains(LocalTime.of(16, 0)));
    }

    @Test
    void parse_weekly_returnsDayAndTime() {
        RecurrencePattern p = parser.parse("WEEKLY:MONDAY:12:00");
        assertEquals(RecurrenceType.WEEKLY, p.getType());
        assertEquals(DayOfWeek.MONDAY, p.getDayOfWeek());
        assertEquals(LocalTime.of(12, 0), p.getTimes().getFirst());
    }

    @Test
    void parse_monthly_returnsDayAndTime() {
        RecurrencePattern p = parser.parse("MONTHLY:15:09:00");
        assertEquals(RecurrenceType.MONTHLY, p.getType());
        assertEquals(15, p.getDayOfMonth());
        assertEquals(LocalTime.of(9, 0), p.getTimes().getFirst());
    }

    @Test
    void parse_once_returnsTime() {
        RecurrencePattern p = parser.parse("ONCE:10:30");
        assertEquals(RecurrenceType.ONCE, p.getType());
        assertEquals(LocalTime.of(10, 30), p.getTimes().getFirst());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"INVALID:PATTERN"})
    void parse_invalidInputs_throw(String pattern) {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(pattern));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MONTHLY:32:10:00", "MONTHLY:0:10:00"})
    void parse_monthlyWithInvalidDay_throws(String pattern) {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(pattern));
    }
}
