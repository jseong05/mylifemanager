package com.mlm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * [기술 설명 / 면접 준비 주석]
 * - 일정 항목을 나타내는 데이터 모델 클래스입니다.
 * - 고유 ID, 일정 제목(todo), 시각(when), 완료 여부(isCompleted), 알람 사전 알림 분(alarmMinutesBefore), 등록 날짜(date), 반복 옵션(repeatOption), 읽기 전용 여부(isReadOnly)를 보관합니다.
 */
public class Schedule {
    private String id;
    private String todo;
    private String when;
    private boolean isCompleted;
    private int alarmMinutesBefore;
    private String date; // "YYYY-MM-DD"
    // 10/31
    // 반복 일정 옵션 ("NONE": 반복없음, "WEEKLY": 매주, "MONTHLY": 매월, "YEARLY": 매년)
    private String repeatOption;

    public Schedule(String id, String todo, String when, boolean isCompleted) {
        this(id, todo, when, isCompleted, 10, getCurrentTodayDate(), "NONE");
    }

    public Schedule(String id, String todo, String when, boolean isCompleted, int alarmMinutesBefore) {
        this(id, todo, when, isCompleted, alarmMinutesBefore, getCurrentTodayDate(), "NONE");
    }

    public Schedule(String id, String todo, String when, boolean isCompleted, int alarmMinutesBefore, String date) {
        this(id, todo, when, isCompleted, alarmMinutesBefore, date, "NONE");
    }

    public Schedule(String id, String todo, String when, boolean isCompleted, int alarmMinutesBefore, String date, String repeatOption) {
        this.id = id;
        this.todo = todo;
        this.when = when;
        this.isCompleted = isCompleted;
        this.alarmMinutesBefore = alarmMinutesBefore > 0 ? alarmMinutesBefore : 10;
        this.date = (date != null && !date.isEmpty()) ? date : getCurrentTodayDate();
        this.repeatOption = (repeatOption != null && !repeatOption.isEmpty()) ? repeatOption : "NONE";
    }

    private static String getCurrentTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public String getId() { return id; }
    public String getTodo() { return todo; }
    public String getWhen() { return when; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public int getAlarmMinutesBefore() { 
        return alarmMinutesBefore > 0 ? alarmMinutesBefore : 10; 
    }
    public void setAlarmMinutesBefore(int alarmMinutesBefore) { 
        this.alarmMinutesBefore = alarmMinutesBefore; 
    }
    public String getDate() {
        return (date == null) ? "" : date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRepeatOption() {
        return (repeatOption != null && !repeatOption.isEmpty()) ? repeatOption : "NONE";
    }

    public void setRepeatOption(String repeatOption) {
        this.repeatOption = repeatOption;
    }

    private boolean isReadOnly = false;

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
    }
}