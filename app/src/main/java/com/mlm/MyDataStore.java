package com.mlm;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyDataStore {
    // 클래스 이름 MyDataStore로 바꿈.. 대충 데이터 저장하는 곳임
    private static final String PREF_NAME = "MLM_Prefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_INITIAL_BALANCE = "initial_balance";
    private static final String KEY_EXPENSES = "expenses";
    private static final String KEY_SCHEDULES = "schedules";
    private static final String KEY_ALLOWANCE_AMOUNT = "allowance_amount";
    private static final String KEY_ALLOWANCE_CYCLE = "allowance_cycle";
    private static final String KEY_ALLOWANCE_DAY = "allowance_day";
    private static final String KEY_USE_GPS = "use_gps_location";
    private static final String KEY_WEATHER_CITY = "weather_city";
    private static final String KEY_DATA_MIGRATED = "data_migrated_v2";

    private SharedPreferences prefs;
    private Gson gson;

    public MyDataStore(Context context) {
        // Activity에서 Context를 받아 SharedPreferences를 초기화!
        // 룸 데이터베이스로 나중에 바꿔야지 입학하면.
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        migrateLegacyData(); // 10/17 기존 데이터를 새 형식에 맞게 한 번만 변환 (구형 데이터 날짜 누락 방지)
    }

    private void migrateLegacyData() {
        // 이미 마이그레이션 했으면 다시 안 함
        if (prefs.getBoolean(KEY_DATA_MIGRATED, false)) return;

        // 일정: date가 없는 옛 데이터는 마이그레이션 시점의 오늘 날짜로 저장
        List<Schedule> schedules = getSchedules();
        boolean changed = false;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        for (Schedule s : schedules) {
            if (s.getDate() == null || s.getDate().isEmpty()) {
                s.setDate(today);
                changed = true;
            }
        }
        if (changed) {
            saveSchedules(schedules);
        }

        prefs.edit().putBoolean(KEY_DATA_MIGRATED, true).apply();
    }

    public void saveUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "사용자");
    }

    public void saveFirstMoney(int balance) {
        prefs.edit().putInt(KEY_INITIAL_BALANCE, balance).apply();
    }

    public int getInitialBalance() {
        return prefs.getInt(KEY_INITIAL_BALANCE, 0);
    }

    public void saveExpenses(List<Expense> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_EXPENSES, json).apply();
    }

    public List<Expense> getExpenses() {
        // 지출 내역 가져오기! 데이터 없으면 그냥 빈 리스트 준다
        String json = prefs.getString(KEY_EXPENSES, null);
        if (json == null) return new ArrayList<>();
        try {
            Type type = new TypeToken<ArrayList<Expense>>() {
            }.getType();
            List<Expense> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveSchedules(List<Schedule> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_SCHEDULES, json).apply();
    }

    /*
            TODO: Gson이 손상된 JSON을 만나면 null을 반환할 수 있으니 방어 코드 추가하기.npe주의하자!!
            현재는 json이 null일 때만 빈 리스트를 반환하지만, 안전하게 한 번 더 체크할 예정임!
                   */
    public List<Schedule> getSchedules() {
        String json = prefs.getString(KEY_SCHEDULES, null);
        if (json == null) return new ArrayList<>();
        try {
            Type type = new TypeToken<ArrayList<Schedule>>() {
            }.getType();
            List<Schedule> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public int totalMoneyNow() {
        // 초기 잔액에 지출이랑 수입 내역을 반영해서 현재 잔액을 계산
        // 후에 날짜별 정산 기능을 추가하면 이 메서드도 수정해야 할듯 보임...
        int balance = getInitialBalance();
        for (Expense e : getExpenses()) {
            if (e.isMinus()) {
                balance -= e.getMoney();
            } else {
                balance += e.getMoney();
            }
        }
        return balance;
    }

    public int getTodayExpenseAmount() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        long startMillis = todayStart.getTimeInMillis();

        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        long endMillis = todayEnd.getTimeInMillis();

        int total = 0;
        for (Expense e : getExpenses()) {
            if (e.isMinus()) {
                long t = e.getTimestamp();
                if (t == 0) continue; // 10/17 날짜 미정인 옛 지출은 오늘 지출에서 제외
                if (t >= startMillis && t <= endMillis) {
                    total += e.getMoney();
                }
            }
        }
        return total;
    }

    public void clearAllData() {
        prefs.edit().clear().apply();
    }

    public void saveAllowanceAmount(int amount) {
        prefs.edit().putInt(KEY_ALLOWANCE_AMOUNT, amount).apply();
    }

    public int getAllowanceAmount() {
        return prefs.getInt(KEY_ALLOWANCE_AMOUNT, 0);
    }

    public void saveAllowanceCycle(String cycle) {
        prefs.edit().putString(KEY_ALLOWANCE_CYCLE, cycle).apply();
    }

    public String getAllowanceCycle() {
        return prefs.getString(KEY_ALLOWANCE_CYCLE, "monthly");
    }

    public void saveAllowanceDay(int day) {
        prefs.edit().putInt(KEY_ALLOWANCE_DAY, day).apply();
    }

    public int getAllowanceDay() {
        return prefs.getInt(KEY_ALLOWANCE_DAY, 0);
    }

    public void saveUseGps(boolean useGps) {
        prefs.edit().putBoolean(KEY_USE_GPS, useGps).apply();
    }

    public boolean getUseGps() {
        return prefs.getBoolean(KEY_USE_GPS, false);
    }

    public void saveWeatherCity(String city) {
        prefs.edit().putString(KEY_WEATHER_CITY, city).apply();
    }

    public String getWeatherCity() {
        return prefs.getString(KEY_WEATHER_CITY, "Seoul");
    }
}