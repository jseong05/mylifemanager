package com.mlm;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mlm.databinding.ItemScheduleBinding;
import java.util.List;

// 일정 리사이클러뷰 목록에 일정 내역을 바인딩하며, AM/PM 12시간제 변환 및 읽기 전용 공휴일 항목을 제어하는 어댑터.
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<Schedule> scheduleList;
    private OnScheduleListener listener;

    public interface OnScheduleListener {
        void onCheckedChanged(Schedule schedule, boolean isChecked);
        void onScheduleClick(Schedule schedule, int position);
    }

    public ScheduleAdapter(List<Schedule> scheduleList, OnScheduleListener listener) {
        this.scheduleList = scheduleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScheduleBinding binding = ItemScheduleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ScheduleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule schedule = scheduleList.get(position);
        holder.binding.tvScheduleTitle.setText(schedule.getTodo());

        String dateStr = schedule.getDate() != null ? schedule.getDate() : "";
        String timeStr = schedule.getWhen();
        if (!schedule.isReadOnly() && timeStr != null && timeStr.contains(":")) {
            timeStr = formatToAmPm(timeStr);
        }

        String repeatStr = "";
        if (schedule.getRepeatOption() != null) {
            switch (schedule.getRepeatOption().toUpperCase(java.util.Locale.ROOT)) {
                case "WEEKLY": repeatStr = " · 매주"; break;
                case "MONTHLY": repeatStr = " · 매월"; break;
                case "YEARLY": repeatStr = " · 매년"; break;
                default: break;
            }
        }

        String subtitle = (!dateStr.isEmpty() ? dateStr + " · " : "") + timeStr + repeatStr;
        holder.binding.tvScheduleTime.setText(subtitle);

        boolean isReadOnly = schedule.isReadOnly();

        if (isReadOnly) {
            holder.binding.cbScheduleComplete.setOnCheckedChangeListener(null);
            holder.binding.cbScheduleComplete.setChecked(false);
            holder.binding.cbScheduleComplete.setEnabled(false);
            holder.binding.tvScheduleTitle.setPaintFlags(
                    holder.binding.tvScheduleTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
            holder.binding.tvScheduleTitle.setAlpha(0.9f);
            holder.binding.tvScheduleTime.setAlpha(0.9f);
        } else {
            holder.binding.cbScheduleComplete.setEnabled(true);
            updateCompletionUi(holder, schedule.isCompleted());

            holder.binding.cbScheduleComplete.setOnCheckedChangeListener(null);
            holder.binding.cbScheduleComplete.setChecked(schedule.isCompleted());

            holder.binding.cbScheduleComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                schedule.setCompleted(isChecked);
                updateCompletionUi(holder, isChecked);
                if (listener != null) {
                    listener.onCheckedChanged(schedule, isChecked);
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (!isReadOnly && listener != null) {
                listener.onScheduleClick(schedule, position);
            }
        });
    }

    private void updateCompletionUi(ScheduleViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.binding.tvScheduleTitle.setPaintFlags(
                    holder.binding.tvScheduleTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
            holder.binding.tvScheduleTitle.setAlpha(0.5f);
            holder.binding.tvScheduleTime.setAlpha(0.5f);
        } else {
            holder.binding.tvScheduleTitle.setPaintFlags(
                    holder.binding.tvScheduleTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
            holder.binding.tvScheduleTitle.setAlpha(1.0f);
            holder.binding.tvScheduleTime.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public void updateData(List<Schedule> newList) {
        this.scheduleList = newList;
        notifyDataSetChanged();
    }

    private String formatToAmPm(String time24) {
        if (time24 == null || !time24.contains(":")) return time24;
        try {
            String[] parts = time24.split(":");
            int hour = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());
            String amPm = hour >= 12 ? "오후" : "오전";
            int hour12 = hour % 12;
            if (hour12 == 0) hour12 = 12;
            return String.format(java.util.Locale.getDefault(), "%s %02d:%02d", amPm, hour12, minute);
        } catch (Exception e) {
            return time24;
        }
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        final ItemScheduleBinding binding;
        ScheduleViewHolder(ItemScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
