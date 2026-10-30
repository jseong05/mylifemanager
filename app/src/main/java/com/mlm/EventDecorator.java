package com.mlm;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import java.util.Collection;
import java.util.HashSet;

///**
//  10/31 개발일지
//  달력 각 날짜 하단에 일정 유무를 알려주는 원(Dot)을 그리기 위한 커스텀 데코레이터
//
public class EventDecorator implements DayViewDecorator {

    private final int color;
    private final HashSet<CalendarDay> dates;

    public EventDecorator(int color, Collection<CalendarDay> dates) {
        this.color = color;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // 날짜 밑에 8dp 크기의 동그라미 점(Dot)을 그림
        view.addSpan(new DotSpan(8f, color));
    }
}
