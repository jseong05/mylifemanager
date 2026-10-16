package com.mlm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Schedule {
    private String id;
    private String todo;
    private String when;
    private boolean isCompleted;
    private int alarmMinutesBefore;
    private String date; // "YYYY-MM-DD"형식으로

    public Schedule(String id, String todo, String when, boolean isCompleted) {
        this(id, todo, when, isCompleted, 10, getCurrentTodayDate());
    }

    public Schedule(String id, String todo, String when, boolean isCompleted, int alarmMinutesBefore) {
        this(id, todo, when, isCompleted, alarmMinutesBefore, getCurrentTodayDate());
    }

    public Schedule(String id, String todo, String when, boolean isCompleted, int alarmMinutesBefore, String date) {
        this.id = id;
        this.todo = todo;
        this.when = when;
        this.isCompleted = isCompleted;
        this.alarmMinutesBefore = alarmMinutesBefore > 0 ? alarmMinutesBefore : 10;
        this.date = (date != null && !date.isEmpty()) ? date : getCurrentTodayDate();
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
        return (date == null) ? "" : date; // 빈 문자열이면 날짜 미정(옛 데이터)
    }

    public void setDate(String date) {
        this.date = date;
    }
}