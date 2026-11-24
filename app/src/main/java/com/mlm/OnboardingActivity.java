package com.mlm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.mlm.databinding.ActivityOnboardingBinding;

/**
 * [기술 설명 / 면접 준비 주석]
 * - 앱 최초 실행 시 4단계 슬라이드를 통해 서비스 소개 및 초기 프로필/자산을 세팅하는 온보딩 마법사 화면입니다.
 * - ViewPager2와 커스텀 동그라미 도트 인디케이터를 사용하며, 입력받은 프로필 정보를 MyDataStore에 저장 후 첫 실행 상태를 완료 처리합니다.
 */
public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new OnboardingAdapter(this::finishOnboarding);
        binding.viewPager.setAdapter(adapter);

        // 초기 인디케이터 점 상태 설정
        updateDots(0);

        // 뷰페이저 페이지 변경 감지
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavigationButtons(position);
                updateDots(position);
            }
        });

        binding.btnPrev.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current > 0) {
                binding.viewPager.setCurrentItem(current - 1, false);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current < adapter.getItemCount() - 1) {
                binding.viewPager.setCurrentItem(current + 1, false);
            } else {
                finishOnboarding();
            }
        });

        binding.tvSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void updateNavigationButtons(int position) {
        binding.btnPrev.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        if (position == adapter.getItemCount() - 1) {
            binding.btnNext.setText("시작하기");
            binding.tvSkip.setVisibility(View.GONE);
        } else {
            binding.btnNext.setText("다음");
            binding.tvSkip.setVisibility(View.VISIBLE);
        }
    }

    private void updateDots(int position) {
        View[] dots = {binding.dot1, binding.dot2, binding.dot3, binding.dot4};
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(i == position ? R.drawable.tab_dot_active : R.drawable.tab_dot_inactive);
        }
    }

    private void finishOnboarding() {
        MyDataStore dataStore = new MyDataStore(this);

        // 입력된 프로필 정보 저장
        String name = adapter.getUserNameInput();
        int balance = adapter.getBalanceInput();
        int allowance = adapter.getAllowanceInput();

        dataStore.saveUserName(name);
        dataStore.saveFirstMoney(balance);
        dataStore.saveAllowanceAmount(allowance);

        // 첫 실행 완료 마킹
        dataStore.setFirstRunCompleted();

        // 메인 화면으로 이동
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
