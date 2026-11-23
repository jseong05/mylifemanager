package com.mlm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final OnStartClickListener onStartClickListener;
    private TextInputEditText etName;
    private TextInputEditText etBalance;
    private TextInputEditText etAllowance;

    public interface OnStartClickListener {
        void onStartClick();
    }

    public OnboardingAdapter(OnStartClickListener listener) {
        this.onStartClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case 0:
                view = inflater.inflate(R.layout.item_onboarding_slide1, parent, false);
                break;
            case 1:
                view = inflater.inflate(R.layout.item_onboarding_slide2, parent, false);
                etName = view.findViewById(R.id.etOnboardingName);
                etBalance = view.findViewById(R.id.etOnboardingBalance);
                etAllowance = view.findViewById(R.id.etOnboardingAllowance);
                break;
            case 2:
                view = inflater.inflate(R.layout.item_onboarding_slide3, parent, false);
                break;
            case 3:
            default:
                view = inflater.inflate(R.layout.item_onboarding_slide4, parent, false);
                View btnStart = view.findViewById(R.id.btnStartApp);
                if (btnStart != null) {
                    btnStart.setOnClickListener(v -> {
                        if (onStartClickListener != null) {
                            onStartClickListener.onStartClick();
                        }
                    });
                }
                break;
        }
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        // 정적 슬라이드 뷰
    }

    public String getUserNameInput() {
        if (etName != null && etName.getText() != null) {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) return name;
        }
        return "사용자";
    }

    public int getBalanceInput() {
        if (etBalance != null && etBalance.getText() != null) {
            try {
                return Integer.parseInt(etBalance.getText().toString().trim());
            } catch (Exception ignored) {}
        }
        return 100000;
    }

    public int getAllowanceInput() {
        if (etAllowance != null && etAllowance.getText() != null) {
            try {
                return Integer.parseInt(etAllowance.getText().toString().trim());
            } catch (Exception ignored) {}
        }
        return 300000;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
