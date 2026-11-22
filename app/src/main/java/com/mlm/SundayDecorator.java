package com.mlm;

import android.text.style.ForegroundColorSpan;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import java.util.Collection;
import java.util.HashSet;

/**
 * 일요일 날짜를 살몬/오렌지 레드(#FF6B6B) 색상으로 표시하는 데코레이터
 */
public class SundayDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> sundayDates;
    private final int color;

    public SundayDecorator(Collection<CalendarDay> dates, int color) {
        this.sundayDates = new HashSet<>(dates);
        this.color = color;
    }

    public SundayDecorator(Collection<CalendarDay> dates) {
        this(dates, 0xFFFF6B6B);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return sundayDates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(color));
    }
}
