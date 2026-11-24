package com.mlm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.mlm.databinding.ActivitySplashBinding;
import java.util.Random;

// 앱 시작 시 로고 애니메이션과 무작위 명언 타이핑 효과를 보여주는 스플래시 화면.
// 첫 실행 여부(isFirstRun)에 따라 온보딩 또는 메인 화면으로 자동 이동합니다.
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable typingRunnable;
    private Runnable navigateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 저장된 다크 모드 설정 적용
        MyDataStore initDataStore = new MyDataStore(this);
        if (initDataStore.getDarkMode()) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        // 1. 중앙 로고 페이드인 & 줌 애니메이션
        binding.ivSplashLogo.setAlpha(0f);
        binding.ivSplashLogo.setScaleX(0.85f);
        binding.ivSplashLogo.setScaleY(0.85f);
        binding.ivSplashLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .start();

        // 2. 28개 명언 중 무작위 1개 선택
        String[] quotes = getResources().getStringArray(R.array.quotes_array);
        String selectedQuote = (quotes.length > 0) 
                ? quotes[new Random().nextInt(quotes.length)] 
                : "\"오늘 하루도 알차고 가치 있게!\"";

        // 3. 한 글자씩 닥닥닥 타이핑 효과 (Typing Effect)
        final int[] charIndex = {0};
        typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (charIndex[0] <= selectedQuote.length()) {
                    binding.tvSplashQuote.setText(selectedQuote.substring(0, charIndex[0]));
                    charIndex[0]++;
                    handler.postDelayed(this, 45); // 45ms 간격 타이핑
                }
            }
        };
        handler.postDelayed(typingRunnable, 350); // 0.35초 후 타이핑 시작

        // 스플래시 화면 이탈 시 첫 실행 여부에 따라 온보딩(OnboardingActivity) 또는 메인(MainActivity) 이동
        navigateRunnable = () -> {
            MyDataStore dataStore = new MyDataStore(SplashActivity.this);
            Class<?> targetActivity = dataStore.isFirstRun() ? OnboardingActivity.class : MainActivity.class;
            startActivity(new Intent(SplashActivity.this, targetActivity));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        };
        handler.postDelayed(navigateRunnable, 2400);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            if (typingRunnable != null) {
                handler.removeCallbacks(typingRunnable);
            }
            if (navigateRunnable != null) {
                handler.removeCallbacks(navigateRunnable);
            }
        }
    }
}
