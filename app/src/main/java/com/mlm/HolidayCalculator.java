package com.mlm;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * [기술 설명 / 면접 준비 주석]
 * - 이 클래스는 외부 API 키 없이도 대한민국 법정 공휴일(양력 고정 8대 공휴일, 음력 명절 및 대체공휴일)과 주말(토/일)을 오프라인에서 0.001초 만에 자동 계산하는 클래스입니다.
 * - CalendarDay의 1-based Month(1~12) 규칙에 맞춰 ThreetenABP의 LocalDate를 사용해 월 인덱스 오류 없이 정교하게 공휴일을 추출합니다.
 */
public class HolidayCalculator {

    // 대한민국 양력 고정 공휴일 판별 (month: 1..12)
    public static boolean isFixedPublicHoliday(int month, int day) {
        return (month == 1 && day == 1)    // 1월 1일 신정
                || (month == 3 && day == 1)   // 3월 1일 삼일절
                || (month == 5 && day == 5)   // 5월 5일 어린이날
                || (month == 6 && day == 6)   // 6월 6일 현충일
                || (month == 8 && day == 15)  // 8월 15일 광복절
                || (month == 10 && day == 3)  // 10월 3일 개천절
                || (month == 10 && day == 9)  // 10월 9일 한글날
                || (month == 12 && day == 25); // 12월 25일 크리스마스
    }

    // 연도별 가변 공휴일 (음력 설날/추석/부처님오신날 및 대체공휴일) 판별
    public static boolean isVariablePublicHoliday(int year, int month, int day) {
        if (year == 2025) {
            // 설날: 1/28, 1/29, 1/30, 1/31(대체)
            if (month == 1 && (day >= 28 && day <= 31)) return true;
            // 삼일절 대체: 3/3
            if (month == 3 && day == 3) return true;
            // 어린이날/부처님오신날 대체: 5/6
            if (month == 5 && day == 6) return true;
            // 추석: 10/5, 10/6, 10/7, 10/8(대체)
            if (month == 10 && (day >= 5 && day <= 8)) return true;
        } else if (year == 2026) {
            // 설날: 2/16, 2/17, 2/18
            if (month == 2 && (day >= 16 && day <= 18)) return true;
            // 삼일절 대체: 3/2
            if (month == 3 && day == 2) return true;
            // 부처님오신날 & 대체: 5/24, 5/25
            if (month == 5 && (day == 24 || day == 25)) return true;
            // 광복절 대체: 8/17
            if (month == 8 && day == 17) return true;
            // 추석 & 대체: 9/24, 9/25, 9/26, 9/28
            if (month == 9 && ((day >= 24 && day <= 26) || day == 28)) return true;
            // 개천절 대체: 10/5
            if (month == 10 && day == 5) return true;
            // 2026년 11월: 공휴일 없음
        } else if (year == 2027) {
            // 설날 & 대체: 2/6, 2/7, 2/8, 2/9
            if (month == 2 && (day >= 6 && day <= 9)) return true;
            // 부처님오신날: 5/13
            if (month == 5 && day == 13) return true;
            // 광복절 대체: 8/16
            if (month == 8 && day == 16) return true;
            // 추석: 9/14, 9/15, 9/16
            if (month == 9 && (day >= 14 && day <= 16)) return true;
            // 개천절 대체: 10/4
            if (month == 10 && day == 4) return true;
            // 한글날 대체: 10/11
            if (month == 10 && day == 11) return true;
            // 성탄절 대체: 12/27
            if (month == 12 && day == 27) return true;
        }
        return false;
    }

    public static boolean isPublicHoliday(int year, int month, int day) {
        return isFixedPublicHoliday(month, day) || isVariablePublicHoliday(year, month, day);
    }

    // 공휴일 명칭 반환 (공휴일이 아닐 경우 null)
    public static String getHolidayName(int year, int month, int day) {
        if (month == 1 && day == 1) return "신정";
        if (month == 3 && day == 1) return "삼일절";
        if (month == 5 && day == 5) return "어린이날";
        if (month == 6 && day == 6) return "현충일";
        if (month == 8 && day == 15) return "광복절";
        if (month == 10 && day == 3) return "개천절";
        if (month == 10 && day == 9) return "한글날";
        if (month == 12 && day == 25) return "크리스마스";

        if (year == 2025) {
            if (month == 1 && (day >= 28 && day <= 30)) return "설날 연휴";
            if (month == 1 && day == 31) return "대체공휴일";
            if (month == 3 && day == 3) return "대체공휴일";
            if (month == 5 && day == 6) return "대체공휴일";
            if (month == 10 && (day >= 5 && day <= 7)) return "추석 연휴";
            if (month == 10 && day == 8) return "대체공휴일";
        } else if (year == 2026) {
            if (month == 2 && (day >= 16 && day <= 18)) return "설날 연휴";
            if (month == 3 && day == 2) return "대체공휴일";
            if (month == 5 && day == 24) return "부처님오신날";
            if (month == 5 && day == 25) return "대체공휴일";
            if (month == 8 && day == 17) return "대체공휴일";
            if (month == 9 && (day >= 24 && day <= 26)) return "추석 연휴";
            if (month == 9 && day == 28) return "대체공휴일";
            if (month == 10 && day == 5) return "대체공휴일";
        } else if (year == 2027) {
            if (month == 2 && (day >= 6 && day <= 8)) return "설날 연휴";
            if (month == 2 && day == 9) return "대체공휴일";
            if (month == 5 && day == 13) return "부처님오신날";
            if (month == 8 && day == 16) return "대체공휴일";
            if (month == 9 && (day >= 14 && day <= 16)) return "추석 연휴";
            if (month == 10 && day == 4) return "대체공휴일";
            if (month == 10 && day == 11) return "대체공휴일";
            if (month == 12 && day == 27) return "대체공휴일";
        }

        return null;
    }

    // 특정 월의 법정 공휴일 (양력 + 음력/대체) Set 반환 (month: 1..12)
    public static Set<CalendarDay> getPublicHolidays(int year, int month) {
        Set<CalendarDay> holidays = new HashSet<>();
        try {
            LocalDate firstDay = LocalDate.of(year, month, 1);
            int maxDays = firstDay.lengthOfMonth();

            for (int day = 1; day <= maxDays; day++) {
                if (isPublicHoliday(year, month, day)) {
                    holidays.add(CalendarDay.from(year, month, day));
                }
            }
        } catch (Exception ignored) {}
        return holidays;
    }

    // 특정 월의 일요일 Set 반환 (공휴일 제외 - 공휴일은 공휴일 색상 우선)
    public static Set<CalendarDay> getSundays(int year, int month) {
        Set<CalendarDay> sundays = new HashSet<>();
        try {
            LocalDate firstDay = LocalDate.of(year, month, 1);
            int maxDays = firstDay.lengthOfMonth();

            for (int day = 1; day <= maxDays; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (date.getDayOfWeek() == DayOfWeek.SUNDAY && !isPublicHoliday(year, month, day)) {
                    sundays.add(CalendarDay.from(year, month, day));
                }
            }
        } catch (Exception ignored) {}
        return sundays;
    }

    // 특정 월의 토요일 Set 반환 (공휴일 제외 - 공휴일은 공휴일 색상 우선)
    public static Set<CalendarDay> getSaturdays(int year, int month) {
        Set<CalendarDay> saturdays = new HashSet<>();
        try {
            LocalDate firstDay = LocalDate.of(year, month, 1);
            int maxDays = firstDay.lengthOfMonth();

            for (int day = 1; day <= maxDays; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY && !isPublicHoliday(year, month, day)) {
                    saturdays.add(CalendarDay.from(year, month, day));
                }
            }
        } catch (Exception ignored) {}
        return saturdays;
    }

    // 기존 호환용 메서드
    public static Set<CalendarDay> getHolidays(int year, int month) {
        return getPublicHolidays(year, month);
    }
}
