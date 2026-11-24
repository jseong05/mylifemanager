package com.mlm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mlm.databinding.ActivityExpenseBinding;
import com.mlm.databinding.DialogAddExpenseBinding;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.List;
import java.util.UUID;

// 가계부 내역(지출/수입)을 관리하는 화면.
// 내역 추가·수정·삭제, 낭비 지출 집계, 키워드 검색 및 상세 필터링(유형, 낭비, 월별, 정렬)을 처리합니다.
public class ExpenseActivity extends AppCompatActivity {
    private ActivityExpenseBinding binding;
    private MyDataStore dataManager;
    private ExpenseAdapter adapter;
    private int selectedTypeFilter = 0; // 0: 전체, 1: 지출만, 2: 수입만
    private int selectedWasteFilter = 0; // 0: 전체, 1: 낭비 지출만
    private int selectedMonthFilter = -1; // -1: 전체 월, 0~11: 1~12월
    private int selectedSortOption = 0; // 0: 최신순, 1: 금액 높은순, 2: 금액 낮은순

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        dataManager = new MyDataStore(this);

        binding.rvExpenseList.setLayoutManager(new LinearLayoutManager(this));
        List<Expense> list = dataManager.getExpenses();
        updateEmptyState(list);
        updateWasteReport();
        adapter = new ExpenseAdapter(list, (expense, position) -> {
            showAddOrEditExpenseDialog(expense, position);
        });
        binding.rvExpenseList.setAdapter(adapter);

        // 필터 버튼 클릭 리스너
        binding.btnExpenseFilter.setOnClickListener(v -> showExpenseFilterDialog());

        // 11/05
        // 가계부 내역 키워드 실시간 검색 및 고급 필터 연동
        binding.etSearchExpense.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExpensesAdvanced();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            // 11/05
            // 검색 필터링 상태에서도 고유 ID 기반으로 정확한 지출 내역을 삭제하도록 수정 (인덱스 불일치 버그 해결)
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense targetExpense = adapter.getItem(position);
                if (targetExpense != null) {
                    List<Expense> currentList = dataManager.getExpenses();
                    for (int i = 0; i < currentList.size(); i++) {
                        if (currentList.get(i).getId().equals(targetExpense.getId())) {
                            currentList.remove(i);
                            break;
                        }
                    }
                    dataManager.saveExpenses(currentList);
                    filterExpensesAdvanced();
                    updateWasteReport();
                    Toast.makeText(ExpenseActivity.this, "내역이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.rvExpenseList);

        
        binding.fabAddExpense.setOnClickListener(v -> showAddOrEditExpenseDialog(null, -1));

        binding.bottomNavigation.setSelectedItemId(R.id.navigation_expense);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                Intent intent = new Intent(this, MainActivity.class);
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
            return id == R.id.navigation_expense;
        });
    }

    private void filterExpensesAdvanced() {
        String query = binding.etSearchExpense.getText() != null ? binding.etSearchExpense.getText().toString().trim() : "";
        List<Expense> allList = dataManager.getExpenses();
        List<Expense> filtered = new java.util.ArrayList<>();

        if (allList != null) {
            for (Expense e : allList) {
                // 1. 검색어 필터
                if (!query.isEmpty() && (e.getMemo() == null || !e.getMemo().toLowerCase().contains(query.toLowerCase()))) {
                    continue;
                }

                // 2. 거래 유형 필터
                if (selectedTypeFilter == 1 && !e.isMinus()) continue;
                if (selectedTypeFilter == 2 && e.isMinus()) continue;

                // 3. 낭비 챌린지 필터
                if (selectedWasteFilter == 1 && (!e.isMinus() || !e.isWaste())) continue;

                // 4. 월 필터
                if (selectedMonthFilter >= 0 && e.getTimestamp() > 0) {
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTimeInMillis(e.getTimestamp());
                    if (c.get(java.util.Calendar.MONTH) != selectedMonthFilter) continue;
                }

                filtered.add(e);
            }
        }

        // 5. 정렬 적용
        if (selectedSortOption == 1) {
            filtered.sort((a, b) -> Integer.compare(b.getMoney(), a.getMoney()));
        } else if (selectedSortOption == 2) {
            filtered.sort((a, b) -> Integer.compare(a.getMoney(), b.getMoney()));
        } else {
            filtered.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        }

        adapter.updateData(filtered);
        updateEmptyState(filtered);
    }

    private void showExpenseFilterDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        TextView tvTypeLabel = new TextView(this);
        tvTypeLabel.setText("거래 유형:");
        layout.addView(tvTypeLabel);

        final android.widget.Spinner spType = new android.widget.Spinner(this);
        String[] types = {"전체 (지출+수입)", "지출 내역만", "수입 내역만"};
        android.widget.ArrayAdapter<String> typeAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);
        spType.setSelection(selectedTypeFilter);
        layout.addView(spType);

        final com.google.android.material.checkbox.MaterialCheckBox cbWasteOnly = new com.google.android.material.checkbox.MaterialCheckBox(this);
        cbWasteOnly.setText("🚨 낭비 지출 내역만 모아보기");
        cbWasteOnly.setChecked(selectedWasteFilter == 1);
        cbWasteOnly.setPadding(0, 16, 0, 0);
        layout.addView(cbWasteOnly);

        TextView tvMonthLabel = new TextView(this);
        tvMonthLabel.setText("조회할 월 선택:");
        tvMonthLabel.setPadding(0, 16, 0, 0);
        layout.addView(tvMonthLabel);

        final android.widget.Spinner spMonth = new android.widget.Spinner(this);
        String[] months = {"전체 월", "1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"};
        android.widget.ArrayAdapter<String> monthAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);
        spMonth.setSelection(selectedMonthFilter + 1);
        layout.addView(spMonth);

        TextView tvSortLabel = new TextView(this);
        tvSortLabel.setText("정렬 방식:");
        tvSortLabel.setPadding(0, 16, 0, 0);
        layout.addView(tvSortLabel);

        final android.widget.Spinner spSort = new android.widget.Spinner(this);
        String[] sorts = {"최신순 (기본)", "금액 높은순", "금액 낮은순"};
        android.widget.ArrayAdapter<String> sortAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sorts);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(sortAdapter);
        spSort.setSelection(selectedSortOption);
        layout.addView(spSort);

        new MaterialAlertDialogBuilder(this)
                .setTitle("🔍 가계부 상세 검색 및 필터")
                .setView(layout)
                .setPositiveButton("적용", (dialog, which) -> {
                    selectedTypeFilter = spType.getSelectedItemPosition();
                    selectedWasteFilter = cbWasteOnly.isChecked() ? 1 : 0;
                    selectedMonthFilter = spMonth.getSelectedItemPosition() - 1;
                    selectedSortOption = spSort.getSelectedItemPosition();
                    filterExpensesAdvanced();
                    Toast.makeText(this, "가계부 필터가 적용되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("필터 초기화", (dialog, which) -> {
                    selectedTypeFilter = 0;
                    selectedWasteFilter = 0;
                    selectedMonthFilter = -1;
                    selectedSortOption = 0;
                    filterExpensesAdvanced();
                    Toast.makeText(this, "가계부 필터가 초기화되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void updateEmptyState(List<Expense> list) {
        boolean empty = list == null || list.isEmpty();
        if (binding != null) {
            binding.tvEmptyExpense.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvExpenseList.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private void updateWasteReport() {
        // 11/04
        // 이번 달 낭비 챌린지 지출 건수 및 누적 금액 리포트 업데이트 (strings.xml 리소스 연동)
        if (binding != null && binding.tvWasteValue != null) {
            int count = dataManager.getMonthlyWasteCount();
            int amount = dataManager.getMonthlyWasteAmount();
            if (count > 0) {
                String formattedAmount = String.format(java.util.Locale.getDefault(), "%,d", amount);
                binding.tvWasteValue.setText(getString(R.string.waste_report_format, count, formattedAmount));
            } else {
                binding.tvWasteValue.setText(getString(R.string.waste_report_zero));
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void showAddOrEditExpenseDialog(Expense editExpense, int position) {
        DialogAddExpenseBinding dialogBinding = DialogAddExpenseBinding.inflate(LayoutInflater.from(this));

        if (editExpense != null) {
            dialogBinding.etExpenseTitle.setText(editExpense.getMemo());
            dialogBinding.etExpenseAmount.setText(String.valueOf(editExpense.getMoney()));
            if (editExpense.isMinus()) {
                dialogBinding.rbExpense.setChecked(true);
                dialogBinding.cbIsWaste.setVisibility(View.VISIBLE);
                dialogBinding.cbIsWaste.setChecked(editExpense.isWaste());
            } else {
                dialogBinding.rbIncome.setChecked(true);
                dialogBinding.cbIsWaste.setChecked(false);
                dialogBinding.cbIsWaste.setVisibility(View.GONE);
            }
        } else {
            dialogBinding.rbExpense.setChecked(true);
            dialogBinding.cbIsWaste.setVisibility(View.VISIBLE);
        }

        // 수입 선택 시 낭비 챌린지 체크박스 숨김 처리
        dialogBinding.rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbIncome) {
                dialogBinding.cbIsWaste.setChecked(false);
                dialogBinding.cbIsWaste.setVisibility(View.GONE);
            } else {
                dialogBinding.cbIsWaste.setVisibility(View.VISIBLE);
            }
        });

        String titleText = (editExpense == null) ? "내역 추가" : "내역 수정";
        String positiveText = (editExpense == null) ? "추가" : "수정";

        new MaterialAlertDialogBuilder(this)
                .setTitle(titleText)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(positiveText, (dialog, which) -> {
                    String memoStr = dialogBinding.etExpenseTitle.getText().toString().trim();
                    String moneyStr = dialogBinding.etExpenseAmount.getText().toString().trim();

                    if (memoStr.isEmpty() || moneyStr.isEmpty()) {
                        Toast.makeText(this, "입력을 다 안했어요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int moneyValue;
                    try {
                        moneyValue = (int) Double.parseDouble(moneyStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "올바른 금액을 입력해 주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean isMinusValue = dialogBinding.rbExpense.isChecked();
                    boolean isWasteValue = isMinusValue && dialogBinding.cbIsWaste.isChecked();

                    List<Expense> currentList = dataManager.getExpenses();
                    if (editExpense == null) {
                        Expense newExpense = new Expense(UUID.randomUUID().toString(), memoStr, moneyValue, isMinusValue, isWasteValue);
                        currentList.add(newExpense);
                        Toast.makeText(this, "내역이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 11/05
                        // 검색 필터링 상태에서도 고유 ID 기반으로 정확한 지출 내역을 수정하도록 보완 (인덱스 덮어쓰기 버그 해결)
                        Expense updatedExpense = new Expense(editExpense.getId(), memoStr, moneyValue, isMinusValue, isWasteValue, editExpense.getTimestamp());
                        for (int i = 0; i < currentList.size(); i++) {
                            if (currentList.get(i).getId().equals(editExpense.getId())) {
                                currentList.set(i, updatedExpense);
                                break;
                            }
                        }
                        Toast.makeText(this, "내역이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    }

                    dataManager.saveExpenses(currentList);
                    filterExpensesAdvanced();
                    updateWasteReport();

                    if (dataManager.totalMoneyNow() < 0) {
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("진심이냐?")
                                .setMessage("잔액이 마이너스인데 지금 이걸 또 쓰는 거냐고!! 정신 차려라 진짜...")
                                .setPositiveButton("반성합니다", null)
                                .show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
