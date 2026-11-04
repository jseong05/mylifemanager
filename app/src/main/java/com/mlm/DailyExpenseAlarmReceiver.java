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
import java.util.Random;

/**
 * // 11/05
 * 매일 밤 9시 오늘 지출이 0원일 경우 사용자에게 지출 입력 리마인더 및 무소비 칭찬 푸시 알림 발송
 */
public class DailyExpenseAlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "mlm_daily_expense_reminder_channel";

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        MyDataStore dataManager = new MyDataStore(context);

        // 오늘 작성된 지출액이 0원인 경우에만 푸시 알림 발송
        if (dataManager.getTodayExpenseAmount() == 0) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "지출 리마인더 알림",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("매일 밤 오늘 지출이 없을 때 보내는 리마인더 알림");
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            Intent mainIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    2001,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 다채로운 리마인더 멘트 모음
            String[] reminders = {
                    "오늘 지출은 0원이었나요? 무소비 성공을 축하해요! 👏",
                    "오늘 혹시 깜빡한 지출이 있다면 자기 전에 가계부에 적어보세요! ✍️",
                    "오늘도 소중한 지갑을 단단히 잘 지켜낸 하루였네요! 💸",
                    "오늘 지출 내역이 없다면 알뜰한 라이프를 칭찬합니다! 🎉"
            };

            String reminderText = reminders[new Random().nextInt(reminders.length)];

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("마이 라이프 메니저")
                    .setContentText(reminderText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(reminderText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            if (notificationManager != null) {
                notificationManager.notify(3001, builder.build());
            }
        }
    }
}
