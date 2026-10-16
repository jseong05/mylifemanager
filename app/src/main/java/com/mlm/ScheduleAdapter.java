package com.mlm;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mlm.databinding.ItemScheduleBinding;
import java.util.List;

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
        holder.binding.tvScheduleTime.setText(schedule.getWhen());
        

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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
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

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        final ItemScheduleBinding binding;
        ScheduleViewHolder(ItemScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
