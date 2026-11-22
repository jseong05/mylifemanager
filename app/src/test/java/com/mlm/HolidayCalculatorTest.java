package com.mlm;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import org.junit.Test;
import java.util.Set;
import static org.junit.Assert.*;

public class HolidayCalculatorTest {

    @Test
    public void testNovember2026Holidays() {
        // 2026년 11월에는 법정 공휴일이 없어야 함
        Set<CalendarDay> publicHolidays = HolidayCalculator.getPublicHolidays(2026, 11);
        assertTrue("2026년 11월은 공휴일이 0개이어야 합니다.", publicHolidays.isEmpty());

        // 2026년 11월 일요일: 1일, 8일, 15일, 22일, 29일 (총 5개)
        Set<CalendarDay> sundays = HolidayCalculator.getSundays(2026, 11);
        assertEquals(5, sundays.size());

        // 2026년 11월 토요일: 7일, 14일, 21일, 28일 (총 4개)
        Set<CalendarDay> saturdays = HolidayCalculator.getSaturdays(2026, 11);
        assertEquals(4, saturdays.size());
    }

    @Test
    public void testFixedAndVariableHolidays() {
        // 2026년 12월 25일 크리스마스 (고정 공휴일)
        Set<CalendarDay> decHolidays = HolidayCalculator.getPublicHolidays(2026, 12);
        boolean hasChristmas = false;
        for (CalendarDay day : decHolidays) {
            if (day.getDay() == 25) {
                hasChristmas = true;
                break;
            }
        }
        assertTrue("크리스마스(12/25)는 공휴일이어야 합니다.", hasChristmas);

        // 2026년 5월 24일 부처님오신날 (가변 공휴일)
        Set<CalendarDay> mayHolidays = HolidayCalculator.getPublicHolidays(2026, 5);
        boolean hasBuddhaDay = false;
        for (CalendarDay day : mayHolidays) {
            if (day.getDay() == 24) {
                hasBuddhaDay = true;
                break;
            }
        }
        assertTrue("부처님오신날(5/24)은 공휴일이어야 합니다.", hasBuddhaDay);
    }
}
