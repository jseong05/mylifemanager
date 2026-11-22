package com.mlm;

import android.text.style.ForegroundColorSpan;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import java.util.Collection;
import java.util.HashSet;

/**
 * 토요일 날짜를 스카이 블루(#64B5F6) 색상으로 표시하는 데코레이터
 */
public class SaturdayDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> saturdayDates;
    private final int color;

    public SaturdayDecorator(Collection<CalendarDay> dates, int color) {
        this.saturdayDates = new HashSet<>(dates);
        this.color = color;
    }

    public SaturdayDecorator(Collection<CalendarDay> dates) {
        this(dates, 0xFF64B5F6);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return saturdayDates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(color));
    }
}
