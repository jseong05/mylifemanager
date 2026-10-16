package com.mlm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mlm.databinding.ActivitySettingsBinding;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private MyDataStore dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //저장된 설정을 불러와서 화면에 체우기
        dataManager = new MyDataStore(this);

        // 월간 스피너 (1일부터 31일까지) 설정
        List<String> monthlyDays = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            monthlyDays.add(i + "일");
        }
        ArrayAdapter<String> monthlyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, monthlyDays
        );
        monthlyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spMonthlyDay.setAdapter(monthlyAdapter);

        // 지급 주기 라디오그룹 리스너
        binding.rgAllowanceCycle.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMonthly) {
                binding.layoutMonthlySelection.setVisibility(View.VISIBLE);
                binding.layoutWeeklySelection.setVisibility(View.GONE);
            } else {
                binding.layoutMonthlySelection.setVisibility(View.GONE);
                binding.layoutWeeklySelection.setVisibility(View.VISIBLE);
            }
        });

        // Load existing settings
        binding.etUserName.setText(dataManager.getUserName());
        binding.etInitialBalance.setText(String.valueOf(dataManager.getInitialBalance()));
        binding.etAllowanceAmount.setText(String.valueOf(dataManager.getAllowanceAmount()));

        String cycle = dataManager.getAllowanceCycle();
        if ("weekly".equals(cycle)) {
            binding.rbWeekly.setChecked(true);
            binding.layoutWeeklySelection.setVisibility(View.VISIBLE);
            binding.layoutMonthlySelection.setVisibility(View.GONE);
        } else {
            binding.rbMonthly.setChecked(true);
            binding.layoutMonthlySelection.setVisibility(View.VISIBLE);
            binding.layoutWeeklySelection.setVisibility(View.GONE);
        }

        int day = dataManager.getAllowanceDay();
        if ("weekly".equals(cycle)) {
            if (day >= 0 && day < binding.spWeeklyDay.getCount()) {
                binding.spWeeklyDay.setSelection(day);
            }
        } else {
            if (day >= 0 && day < 31) {
                binding.spMonthlyDay.setSelection(day);
            }
        }

        // 날씨/위치 설정 세팅
        boolean useGps = dataManager.getUseGps();
        binding.switchUseGps.setChecked(useGps);
        binding.layoutCitySelection.setVisibility(useGps ? View.GONE : View.VISIBLE);

        binding.switchUseGps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.layoutCitySelection.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        String currentCity = dataManager.getWeatherCity();
        String[] cityValues = getResources().getStringArray(R.array.cities_value);
        for (int i = 0; i < cityValues.length; i++) {
            if (cityValues[i].equalsIgnoreCase(currentCity)) {
                binding.spCity.setSelection(i);
                break;
            }
        }

        binding.btnSaveSettings.setOnClickListener(v -> {
            String name = binding.etUserName.getText().toString().trim();
            String balanceStr = binding.etInitialBalance.getText().toString().trim();
            String allowanceStr = binding.etAllowanceAmount.getText().toString().trim();

            if (name.isEmpty() || balanceStr.isEmpty()) {
                Toast.makeText(this, "이름과 잔액을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            int balance = Integer.parseInt(balanceStr);
            int allowance = allowanceStr.isEmpty() ? 0 : Integer.parseInt(allowanceStr);

            boolean isWeekly = binding.rbWeekly.isChecked();
            String saveCycle = isWeekly ? "weekly" : "monthly";
            int saveDay = isWeekly ? binding.spWeeklyDay.getSelectedItemPosition() : binding.spMonthlyDay.getSelectedItemPosition();

            boolean saveUseGps = binding.switchUseGps.isChecked();
            int cityPos = binding.spCity.getSelectedItemPosition();
            String[] cityArray = getResources().getStringArray(R.array.cities_value);
            String saveCity = (cityPos >= 0 && cityPos < cityArray.length) ? cityArray[cityPos] : "Seoul";

            dataManager.saveUserName(name);
            dataManager.saveFirstMoney(balance);
            dataManager.saveAllowanceAmount(allowance);
            dataManager.saveAllowanceCycle(saveCycle);
            dataManager.saveAllowanceDay(saveDay);
            dataManager.saveUseGps(saveUseGps);
            dataManager.saveWeatherCity(saveCity);

            Toast.makeText(this, "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });

        binding.btnResetData.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_reset_title)
                    .setMessage(R.string.dialog_reset_message)
                    .setPositiveButton(R.string.btn_reset_confirm, (dialog, which) -> {
                        dataManager.clearAllData();
                        Toast.makeText(this, "모든 데이터가 초기화되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

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
            } else if (id == R.id.navigation_schedule) {
                Intent intent = new Intent(this, ScheduleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
