package com.mlm;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.mlm.databinding.ActivityMainBinding;
import androidx.activity.OnBackPressedCallback; // 10/23 뒤로가기 콜백
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private long backKeyPressedTime = 0; // 10/23 뒤로가기 버튼 누른 시각 저장
    private ActivityMainBinding binding;
    private MyDataStore dataManager;
    private ScheduleAdapter sAdapter;
    private ExpenseAdapter expenseAdapter;

    // 위치 권한을 한 번만 요청하기 위한 플래그 (다시 켤 때마다 팝업 반복 방지)
    // 10/17
    // 위치 권한 매번 묻는 거 귀찮아서 런처 플래그 추가함.
    // 거부하면 지알아서 설정한 도시 날씨로 펄 백 되게 든든하게 막아둠.
    private boolean locationPermissionRequested = false;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (granted) {
                    // 권한 얻었으니 다시 날씨 갱신 -> 이번엔 GPS 경로로 진행됨
                    updateWeatherInfo();
                } else {
                    // 거부됨 -> 설정한 도시 날씨로 대체
                    fetchFallbackCityWeather();
                    Toast.makeText(this, "위치 권한이 없어 설정한 도시 날씨를 표시할게요", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 앱 전체적인 부분에서 데이터를 관리하는 저장소 (SharedPreferences+Gson)
        dataManager = new MyDataStore(this);


        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Initialize RecyclerViews
        binding.rvTodaySchedule.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentExpense.setLayoutManager(new LinearLayoutManager(this));

        sAdapter = new ScheduleAdapter(new ArrayList<>(), new ScheduleAdapter.OnScheduleListener() {
            @Override
            public void onCheckedChanged(Schedule schedule, boolean isChecked) {
                List<Schedule> list = dataManager.getSchedules();
                for (Schedule s : list) {
                    if (s.getId().equals(schedule.getId())) {
                        s.setCompleted(isChecked);
                        break;
                    }
                }
                dataManager.saveSchedules(list);
                screenRefresh();
            }

            @Override
            public void onScheduleClick(Schedule schedule, int position) {
                Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        expenseAdapter = new ExpenseAdapter(new ArrayList<>(), (expense, position) -> {
            Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        binding.rvTodaySchedule.setAdapter(sAdapter);
        binding.rvRecentExpense.setAdapter(expenseAdapter);

        binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_expense) {
                Intent intent = new Intent(this, ExpenseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_schedule) {
                Intent intent = new Intent(this, ScheduleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return id == R.id.navigation_home;
        });

        binding.btnAddSchedule.setOnClickListener(view -> {
            Intent intent = new Intent(this, ScheduleActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        binding.btnAddExpense.setOnClickListener(view -> {
            Intent intent = new Intent(this, ExpenseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        binding.btnSettings.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // 10/23
        // 메인 화면에서 뒤로가기 버튼 2번 누르면 앱 종료 처리
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (System.currentTimeMillis() > backKeyPressedTime + 2000) { // 2sec 이내로 다시 누르지 않을 경우
                    backKeyPressedTime = System.currentTimeMillis();
                    Toast.makeText(MainActivity.this, "뒤로가기 버튼을 한번 더 누르면 앱이 종료 됩니다.", Toast.LENGTH_SHORT).show(); // 메시지 띄우기
                    return;
                }
                // 2sec 안에 누르면 앱이 종료됨.
                if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                    finish();
                }
            }
        });
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        screenRefresh();
    }

    private void screenRefresh() {
        // onResume마다 호출해서 저장된 데이터를 화면에 다시 반영
        // 다른 화면에서 저장하고 돌아왔을 때 최신 상태를 보여주기 위함임!

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Locale.KOREAN);
        String todayDate = sdf.format(new Date());
        binding.tvWelcome.setText("반가워요, " + dataManager.getUserName() + "님!");
        binding.tvDate.setText(todayDate);

        int currentBalance = dataManager.totalMoneyNow();
        binding.tvBalanceValue.setText(
                String.format("%,d원", currentBalance));

        int todayExpense = dataManager.getTodayExpenseAmount();
        binding.tvHomeExpenseToday.setText(String.format("오늘 지출: %,d원", todayExpense));

        // 날씨 정보 호출 (GPS 또는 설정 도시 기반)
        updateWeatherInfo();

        // D-Day 및 실질적 권장 지출 계산
        Calendar now = Calendar.getInstance();
        int currentDayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        int targetMonthlyDay = dataManager.getAllowanceDay() + 1; // 1 ~ 31
        int daysLeft;
        if (targetMonthlyDay >= currentDayOfMonth) {
            daysLeft = targetMonthlyDay - currentDayOfMonth;
        } else {
            Calendar nextMonth = (Calendar) now.clone();
            nextMonth.add(Calendar.MONTH, 1);
            int maxDay = nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
            int target = Math.min(targetMonthlyDay, maxDay);
            nextMonth.set(Calendar.DAY_OF_MONTH, target);
            long diff = nextMonth.getTimeInMillis() - now.getTimeInMillis();
            daysLeft = (int) (diff / (1000 * 60 * 60 * 24));
        }
        if (daysLeft <= 0) daysLeft = 1;

        int recommendedDaily = currentBalance / daysLeft;
        binding.tvDDay.setText("용돈 받는 날까지 D-" + daysLeft);
        if (currentBalance < 0) {
            binding.tvDailyRecommended.setText("잔액 마이너스인데 진심이냐? 정신 차려라!!");
            binding.tvDailyRecommended.setTextColor(0xFFFF5252);
        } else {
            binding.tvDailyRecommended.setText(String.format(Locale.getDefault(), "오늘 권장 지출액: %,d원", recommendedDaily));
            binding.tvDailyRecommended.setTextColor(0xFF1A73E8);
        }
// 오늘 날짜 기준으로 일정 필터링
        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateSdf.format(new Date());

        List<Schedule> allSchedules = dataManager.getSchedules();
        List<Schedule> todaySchedules = new ArrayList<>();
        for (Schedule s : allSchedules) {
            if (today.equals(s.getDate())) {
                todaySchedules.add(s);
            }
        }
        sAdapter.updateData(todaySchedules);
        binding.tvScheduleTitle.setText("오늘의 일정 (" + todaySchedules.size() + ")");
//10/17
     //제목은 "오늘의 일정"이지만 실제로는 모든 날짜의 일정을 다 보여주고, 개수도 전체 개수로 표시되는 버그 아닌 버그가 있었음.
//Schedule에 date("YYYY-MM-DD")가 생겼으니, 홈에서도 이걸 기준으로 걸러주는 것임.
        List<Expense> expenses = dataManager.getExpenses();
        expenseAdapter.updateData(expenses);
    }

    private void updateWeatherInfo() {
        // 10/16
        // 잔액 카드에 날씨 텍스트 찌그러지는 거 보기 싫어서 상단에 Chip으로 독립시킴.
        // GPS 쓸지 수동 도시 선택 쓸지 토글 기능 연동 완료!
        if (dataManager.getUseGps()) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                // 기존 코드 그대로: LocationManager로 위치 가져와서 WeatherClient.fetchWeatherByLocation()
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = null;
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location == null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }

                if (location != null) {
                    WeatherClient.fetchWeatherByLocation(location.getLatitude(), location.getLongitude(), new WeatherClient.WeatherCallback() {
                        @Override
                        public void onSuccess(String weatherInfo) {
                            binding.chipWeather.setText(weatherInfo);
                        }

                        @Override
                        public void onError(String error) {
                            fetchFallbackCityWeather();
                        }
                    });
                } else {
                    fetchFallbackCityWeather();
                }
            } else if (!locationPermissionRequested) {
                // 권한이 없으면 팝업 요청 (한 번만)
                locationPermissionRequested = true;
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            } else {
                // 이미 한 번 거부했으면 다시 안 물어보고 도시 날씨로
                fetchFallbackCityWeather();
            }
        } else {
            fetchFallbackCityWeather();
        }
    }

    private void fetchFallbackCityWeather() {
        String city = dataManager.getWeatherCity();
        WeatherClient.fetchWeatherByCity(city, new WeatherClient.WeatherCallback() {
            @Override
            public void onSuccess(String weatherInfo) {
                binding.chipWeather.setText(weatherInfo);
            }

            @Override
            public void onError(String error) {
                binding.chipWeather.setText("🌤️ 날씨 정보를 불러올 수 없음");
            }
        });
    }
}