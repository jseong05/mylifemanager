package com.mlm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.mlm.databinding.ActivityScheduleBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ScheduleActivity extends AppCompatActivity {
    private ActivityScheduleBinding binding;
    private MyDataStore dataManager;
    private ScheduleAdapter adapter;
    private String selectedDate; // "YYYY-MM-DD" 포맷 선택된 날짜

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 오늘 날짜로 초기화
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(new Date());

        // 알림 권한 요청 (Android 13이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        dataManager = new MyDataStore(this);

        binding.rvScheduleList.setLayoutManager(new LinearLayoutManager(this));
        
        // 초기 어댑터 연결
        adapter = new ScheduleAdapter(new ArrayList<>(), new ScheduleAdapter.OnScheduleListener() {
            @Override
            public void onCheckedChanged(Schedule schedule, boolean isChecked) {
                List<Schedule> currentList = dataManager.getSchedules();
                for (Schedule s : currentList) {
                    if (s.getId().equals(schedule.getId())) {
                        s.setCompleted(isChecked);
                        break;
                    }
                }
                dataManager.saveSchedules(currentList);
            }

            @Override
            public void onScheduleClick(Schedule schedule, int position) {
                showAddOrEditScheduleDialog(schedule, position);
            }
        });
        binding.rvScheduleList.setAdapter(adapter);

        // 상시 달력 날짜 변경 리스너
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            filterSchedulesByDate(selectedDate);
        });

        // 오늘 날짜 일정 로딩
        filterSchedulesByDate(selectedDate);

        // CRUD: Swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                List<Schedule> filteredList = getFilteredSchedules(selectedDate);
                if (position >= 0 && position < filteredList.size()) {
                    Schedule removedSchedule = filteredList.get(position);
                    
                    // 전체 리스트에서 제거
                    List<Schedule> allList = dataManager.getSchedules();
                    for (int i = 0; i < allList.size(); i++) {
                        if (allList.get(i).getId().equals(removedSchedule.getId())) {
                            allList.remove(i);
                            break;
                        }
                    }
                    cancelScheduleAlarm(removedSchedule.getId());
                    dataManager.saveSchedules(allList);
                    filterSchedulesByDate(selectedDate);
                    Toast.makeText(ScheduleActivity.this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.rvScheduleList);

        // CRUD: Dialog to add
        binding.fabAddSchedule.setOnClickListener(v -> showAddOrEditScheduleDialog(null, -1));

        binding.bottomNavigation.setSelectedItemId(R.id.navigation_schedule);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_expense) {
                Intent intent = new Intent(this, ExpenseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return id == R.id.navigation_schedule;
        });
    }

    private List<Schedule> getFilteredSchedules(String date) {
        List<Schedule> all = dataManager.getSchedules();
        List<Schedule> filtered = new ArrayList<>();
        if (all != null) {
            for (Schedule s : all) {
                if (date.equals(s.getDate())) {
                    filtered.add(s);
                }
            }
        }
        return filtered;
    }

    private void filterSchedulesByDate(String date) {
        List<Schedule> filtered = getFilteredSchedules(date);
        adapter.updateData(filtered);
        updateEmptyState(filtered);
    }

    private void updateEmptyState(List<Schedule> list) {
        boolean empty = list == null || list.isEmpty();
        if (binding != null) {
            binding.tvEmptySchedule.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvScheduleList.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void showAddOrEditScheduleDialog(Schedule editSchedule, int position) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etTodo = new EditText(this);
        etTodo.setHint("일정 제목 (예: 팀 미팅)");
        if (editSchedule != null) etTodo.setText(editSchedule.getTodo());
        layout.addView(etTodo);

        // 기본 시간 설정값 (시간 미선택 시 현재 시각으로 자동 적용)
        final int[] selectedHour = {Calendar.getInstance().get(Calendar.HOUR_OF_DAY)};
        final int[] selectedMinute = {Calendar.getInstance().get(Calendar.MINUTE)};

        if (editSchedule != null && editSchedule.getWhen() != null && editSchedule.getWhen().contains(":")) {
            try {
                String[] parts = editSchedule.getWhen().split(":");
                selectedHour[0] = Integer.parseInt(parts[0].trim());
                selectedMinute[0] = Integer.parseInt(parts[1].trim());
            } catch (Exception ignored) {}
        }

        // 시간 선택 버튼 (MaterialTimePicker 호출)
        MaterialButton btnSelectTime = new MaterialButton(this);
        btnSelectTime.setPadding(0, 20, 0, 20);
        String initialTimeText = String.format(Locale.getDefault(), "일정 시간: %02d:%02d (클릭하여 변경)", selectedHour[0], selectedMinute[0]);
        btnSelectTime.setText(initialTimeText);

        btnSelectTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(selectedHour[0])
                    .setMinute(selectedMinute[0])
                    .setTitleText("일정 시간 선택")
                    .build();

            timePicker.addOnPositiveButtonClickListener(dialogView -> {
                selectedHour[0] = timePicker.getHour();
                selectedMinute[0] = timePicker.getMinute();
                String formattedTime = String.format(Locale.getDefault(), "일정 시간: %02d:%02d (클릭하여 변경)", selectedHour[0], selectedMinute[0]);
                btnSelectTime.setText(formattedTime);
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        layout.addView(btnSelectTime);

        // 알람 설정 레이아웃 (Material 3 ChipGroup 적용)
        TextView tvAlarmLabel = new TextView(this);
        tvAlarmLabel.setText("알람 미리 알림 설정:");
        tvAlarmLabel.setTextSize(14f);
        tvAlarmLabel.setPadding(0, 20, 0, 8);
        layout.addView(tvAlarmLabel);

        final int[] alarmOffset = {editSchedule != null ? editSchedule.getAlarmMinutesBefore() : 10};

        TextView tvAlarmInfo = new TextView(this);
        tvAlarmInfo.setText(formatAlarmText(alarmOffset[0]));
        tvAlarmInfo.setTextSize(13f);
        tvAlarmInfo.setPadding(0, 0, 0, 8);
        layout.addView(tvAlarmInfo);

        // Material 3 ChipGroup 생성
        // 10/17
        // 알람 설정 버튼들 다 치우고 Material 3 ChipGroup으로 싹 갈아엎음.
        // 정시/10분/30분/1시간 전은 물론이고 사용자 지정 TimePicker까지 완벽 연동.
        // 시간이랑 분 미선택 시 현재 시각으로 자동 세팅되게 방어 코드도 넣음.
        ChipGroup chipGroup = new ChipGroup(this);
        chipGroup.setSingleSelection(true);

        int[] offsets = {0, 10, 30, 60};
        String[] labels = {"정시 알림", "10분 전", "30분 전", "1시간 전"};

        for (int i = 0; i < offsets.length; i++) {
            final int offsetVal = offsets[i];
            Chip chip = new Chip(this);
            chip.setText(labels[i]);
            chip.setCheckable(true);
            if (alarmOffset[0] == offsetVal) chip.setChecked(true);

            chip.setOnClickListener(v -> {
                alarmOffset[0] = offsetVal;
                tvAlarmInfo.setText(formatAlarmText(alarmOffset[0]));
            });
            chipGroup.addView(chip);
        }

        // 사용자 지정 미리 알림 (MaterialTimePicker 이용)
        Chip customChip = new Chip(this);
        customChip.setText("사용자 지정...");
        customChip.setCheckable(true);
        if (alarmOffset[0] != 0 && alarmOffset[0] != 10 && alarmOffset[0] != 30 && alarmOffset[0] != 60) {
            customChip.setChecked(true);
        }

        customChip.setOnClickListener(v -> {
            int initH = alarmOffset[0] / 60;
            int initM = alarmOffset[0] % 60;

            MaterialTimePicker customPicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(initH)
                    .setMinute(initM)
                    .setTitleText("몇 시간 몇 분 전에 알릴까요?")
                    .build();

            customPicker.addOnPositiveButtonClickListener(dialogView -> {
                int customMinutes = customPicker.getHour() * 60 + customPicker.getMinute();
                if (customMinutes <= 0) customMinutes = 10;
                alarmOffset[0] = customMinutes;
                customChip.setChecked(true);
                tvAlarmInfo.setText(formatAlarmText(alarmOffset[0]));
            });

            customPicker.show(getSupportFragmentManager(), "CUSTOM_ALARM_PICKER");
        });

        chipGroup.addView(customChip);
        layout.addView(chipGroup);

        String titleText = (editSchedule == null) ? "일정 추가" : "일정 수정";
        String positiveText = (editSchedule == null) ? "추가" : "수정";

        new AlertDialog.Builder(this)
                .setTitle(titleText)
                .setView(layout)
                .setPositiveButton(positiveText, (dialog, which) -> {
                    String todoText = etTodo.getText().toString().trim();

                    if (todoText.isEmpty()) {
                        Toast.makeText(this, "일정 제목을 입력해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String whenText = String.format(Locale.getDefault(), "%02d:%02d", selectedHour[0], selectedMinute[0]);

                    List<Schedule> allList = dataManager.getSchedules();
                    if (editSchedule == null) {
                        Schedule newSchedule = new Schedule(UUID.randomUUID().toString(), todoText, whenText, false, alarmOffset[0], selectedDate);
                        allList.add(newSchedule);
                        setScheduleAlarm(newSchedule.getId(), todoText, selectedDate, whenText, alarmOffset[0]);
                        Toast.makeText(this, "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Schedule updatedSchedule = new Schedule(editSchedule.getId(), todoText, whenText, editSchedule.isCompleted(), alarmOffset[0], editSchedule.getDate());
                        
                        for (int i = 0; i < allList.size(); i++) {
                            if (allList.get(i).getId().equals(editSchedule.getId())) {
                                allList.set(i, updatedSchedule);
                                break;
                            }
                        }
                        setScheduleAlarm(updatedSchedule.getId(), todoText, editSchedule.getDate(), whenText, alarmOffset[0]);
                        Toast.makeText(this, "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    }

                    dataManager.saveSchedules(allList);
                    filterSchedulesByDate(selectedDate);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private String formatAlarmText(int minutes) {
        if (minutes <= 0) return "알람 설정: 일정 정시에 알림";
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0 && m > 0) {
            return String.format(Locale.getDefault(), "알람 설정: 일정 %d시간 %d분 전 알림", h, m);
        } else if (h > 0) {
            return String.format(Locale.getDefault(), "알람 설정: 일정 %d시간 전 알림", h);
        } else {
            return String.format(Locale.getDefault(), "알람 설정: 일정 %d분 전 알림", m);
        }
    }

    private void cancelScheduleAlarm(String scheduleId) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, ScheduleAlarmReceiver.class);
            int requestCode = scheduleId.hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (alarmManager != null && pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setScheduleAlarm(String scheduleId, String todo, String dateText, String whenText, int minutesBefore) {
        try {
            String[] dateParts = dateText.split("-");
            String[] timeParts = whenText.split(":");
            if (dateParts.length == 3 && timeParts.length == 2) {
                int year = Integer.parseInt(dateParts[0].trim());
                int month = Integer.parseInt(dateParts[1].trim()) - 1;
                int day = Integer.parseInt(dateParts[2].trim());
                int hour = Integer.parseInt(timeParts[0].trim());
                int minute = Integer.parseInt(timeParts[1].trim());

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day, hour, minute, 0);

                calendar.add(Calendar.MINUTE, -minutesBefore);

                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    return;
                }

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, ScheduleAlarmReceiver.class);
                intent.putExtra(ScheduleAlarmReceiver.EXTRA_TITLE, todo);
                intent.putExtra("EXTRA_MINUTES_BEFORE", minutesBefore);

                int requestCode = scheduleId.hashCode();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        } else {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
