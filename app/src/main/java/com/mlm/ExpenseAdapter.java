package com.mlm;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mlm.databinding.ItemExpenseBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense, int position);
    }

    public ExpenseAdapter(List<Expense> expenseList, OnExpenseClickListener listener) {
        this.expenseList = expenseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.binding.tvExpenseTitle.setText(expense.getMemo());

        // 10/17 날짜 미정(timestamp == 0)인 옛 지출 데이터 방어 코드
        if (expense.getTimestamp() == 0) {
            holder.binding.tvExpenseDate.setText("날짜 정보 없음");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 HH:mm", Locale.KOREAN);
            holder.binding.tvExpenseDate.setText(sdf.format(new Date(expense.getTimestamp())));
        }

        String amountText = (expense.isMinus() ? "-" : "+") + expense.getMoney() + "원";
        holder.binding.tvExpenseAmount.setText(amountText);
        holder.binding.tvExpenseAmount.setTextColor(
                expense.isMinus() ? 0xFFFF5252 : 0xFF2196F3
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(expense, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void updateData(List<Expense> newList) {
        this.expenseList = newList;
        notifyDataSetChanged();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        final ItemExpenseBinding binding;
        ExpenseViewHolder(ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}