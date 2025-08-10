package com.treatment.schedulerservice.unit.service;

import com.treatment.schedulerservice.helper.RecurrencePattern;
import com.treatment.schedulerservice.service.RecurrencePatternService;
import com.treatment.schedulerservice.service.recurrence.OccurrenceGenerator;
import com.treatment.schedulerservice.service.recurrence.RecurrencePatternParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurrencePatternServiceDelegationTest {

    @Mock
    RecurrencePatternParser parser;
    @Mock
    OccurrenceGenerator generator;
    @InjectMocks
    RecurrencePatternService service;

    @Test
    void parsePattern_delegatesToParser() {
        service.parsePattern("DAILY:08:00");
        verify(parser).parse("DAILY:08:00");
    }

    @Test
    void getNextOccurrences_delegatesToGenerator() {
        RecurrencePattern pattern = RecurrencePattern.daily(List.of(LocalTime.of(8,0)));
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusDays(1);

        service.getNextOccurrences(pattern, from, to, null, null);

        verify(generator).generate(eq(pattern), eq(from), eq(to), isNull(), isNull());
    }
}
