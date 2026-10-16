package com.mlm;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class ScheduleAlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "mlm_schedule_channel";
    public static final String EXTRA_TITLE = "extra_title";

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String rawTitle = intent.getStringExtra(EXTRA_TITLE);
        String title = (rawTitle != null) ? rawTitle : "일정 알림";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "일정 알림 채널",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("마이 라이프 메니저 일정 알림");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int minutesBefore = intent.getIntExtra("EXTRA_MINUTES_BEFORE", 0);
        String text = (minutesBefore > 0)
                ? "일정 " + minutesBefore + "분 전 알림: " + title
                : "예정된 일정: " + title;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentTitle("마이 라이프 메니저")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
