package com.mlm;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * // 11/17
 * 주말(토요일/일요일) 및 대한민국 양력 8대 고정 공휴일 자동 계산 클래스 (API 키 필요 없음)
 */
public class HolidayCalculator {

    public static Set<CalendarDay> getHolidays(int year, int month) {
        Set<CalendarDay> holidays = new HashSet<>();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month); // 0-based
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= maxDays; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            // 토요일(7) 또는 일요일(1)
            boolean isWeekend = (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY);

            // 양력 8대 고정 공휴일 (신정, 삼일절, 어린이날, 현충일, 광복절, 개천절, 한글날, 크리스마스)
            boolean isFixedHoliday = (month == 0 && day == 1)   // 1월 1일 신정
                    || (month == 2 && day == 1)  // 3월 1일 삼일절
                    || (month == 4 && day == 5)  // 5월 5일 어린이날
                    || (month == 5 && day == 6)  // 6월 6일 현충일
                    || (month == 7 && day == 15) // 8월 15일 광복절
                    || (month == 9 && day == 3)  // 10월 3일 개천절
                    || (month == 9 && day == 9)  // 10월 9일 한글날
                    || (month == 11 && day == 25); // 12월 25일 크리스마스

            if (isWeekend || isFixedHoliday) {
                try {
                    holidays.add(CalendarDay.from(year, month, day));
                } catch (Exception ignored) {}
            }
        }

        return holidays;
    }
}
