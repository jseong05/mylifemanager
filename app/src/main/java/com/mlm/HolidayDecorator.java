package com.mlm;

import android.text.style.ForegroundColorSpan;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;

/**
 * // 11/17
 * 주말 및 공휴일 날짜 글씨 색상을 빨간색(일요일/공휴일) 및 파란색(토요일)으로 표시하는 데코레이터
 */
public class HolidayDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> holidayDates;

    public HolidayDecorator(Collection<CalendarDay> dates) {
        this.holidayDates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return holidayDates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // 날짜 텍스트를 빨간색으로 변경
        view.addSpan(new ForegroundColorSpan(0xFFFF5252));
    }
}
