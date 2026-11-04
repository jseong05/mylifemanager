package com.mlm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
import java.util.List;
import java.util.UUID;

public class ExpenseActivity extends AppCompatActivity {
    private ActivityExpenseBinding binding;
    private MyDataStore dataManager;
    private ExpenseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dataManager = new MyDataStore(this);

        binding.rvExpenseList.setLayoutManager(new LinearLayoutManager(this));
        List<Expense> list = dataManager.getExpenses();
        updateEmptyState(list);
        updateWasteReport();
        adapter = new ExpenseAdapter(list, (expense, position) -> {
            showAddOrEditExpenseDialog(expense, position);
        });
        binding.rvExpenseList.setAdapter(adapter);

        // 11/05
        // 가계부 내역 키워드 실시간 검색 (TextWatcher 연동)
        binding.etSearchExpense.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExpenses(s.toString().trim());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                List<Expense> currentList = dataManager.getExpenses();
                currentList.remove(position);
                dataManager.saveExpenses(currentList);
                adapter.updateData(currentList);
                updateEmptyState(currentList);
                updateWasteReport();
                Toast.makeText(ExpenseActivity.this, "내역이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
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

    private void filterExpenses(String query) {
        // 11/05
        // 가계부 검색어에 따라 메모(제목) 키워드 필터링
        List<Expense> allList = dataManager.getExpenses();
        if (query == null || query.isEmpty()) {
            adapter.updateData(allList);
            updateEmptyState(allList);
            return;
        }

        List<Expense> filtered = new java.util.ArrayList<>();
        for (Expense e : allList) {
            if (e.getMemo() != null && e.getMemo().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(e);
            }
        }
        adapter.updateData(filtered);
        updateEmptyState(filtered);
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
            } else {
                dialogBinding.rbIncome.setChecked(true);
            }
            dialogBinding.cbIsWaste.setChecked(editExpense.isWaste());
        }

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
                    boolean isWasteValue = dialogBinding.cbIsWaste.isChecked();

                    List<Expense> currentList = dataManager.getExpenses();
                    if (editExpense == null) {
                        Expense newExpense = new Expense(UUID.randomUUID().toString(), memoStr, moneyValue, isMinusValue, isWasteValue);
                        currentList.add(newExpense);
                        Toast.makeText(this, "내역이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Expense updatedExpense = new Expense(editExpense.getId(), memoStr, moneyValue, isMinusValue, isWasteValue, editExpense.getTimestamp());
                        currentList.set(position, updatedExpense);
                        Toast.makeText(this, "내역이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    }

                    dataManager.saveExpenses(currentList);
                    adapter.updateData(currentList);
                    updateEmptyState(currentList);
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
