package com.mlm;

import android.text.style.ForegroundColorSpan;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import java.util.Collection;
import java.util.HashSet;

/**
 * 대한민국 법정 공휴일(대체공휴일 포함) 날짜를 코랄 레드(#E57373) 색상으로 표시하는 데코레이터
 */
public class HolidayDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> holidayDates;
    private final int color;

    public HolidayDecorator(Collection<CalendarDay> dates, int color) {
        this.holidayDates = new HashSet<>(dates);
        this.color = color;
    }

    public HolidayDecorator(Collection<CalendarDay> dates) {
        this(dates, 0xFFE57373); // 코랄 레드
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return holidayDates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(color));
    }
}
