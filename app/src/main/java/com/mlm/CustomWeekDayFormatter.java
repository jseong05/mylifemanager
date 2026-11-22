package com.mlm;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter;
import org.threeten.bp.DayOfWeek;

/**
 * 상단 요일 헤더('일', '토') 텍스트 색상을 일요일/토요일 지정 색상과 일관되게 표시하는 포맷터
 */
public class CustomWeekDayFormatter implements WeekDayFormatter {

    private final int sundayColor;
    private final int saturdayColor;

    public CustomWeekDayFormatter(int sundayColor, int saturdayColor) {
        this.sundayColor = sundayColor;
        this.saturdayColor = saturdayColor;
    }

    @Override
    public CharSequence format(DayOfWeek dayOfWeek) {
        String[] labels = {"월", "화", "수", "목", "금", "토", "일"};
        int index = dayOfWeek.getValue() - 1; // Monday = 1 -> index 0, Sunday = 7 -> index 6
        String label = labels[index];

        SpannableString spannable = new SpannableString(label);
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            spannable.setSpan(new ForegroundColorSpan(sundayColor), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            spannable.setSpan(new ForegroundColorSpan(saturdayColor), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }
}
