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
}