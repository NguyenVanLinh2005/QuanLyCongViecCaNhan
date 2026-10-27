package com.example.quanlycongvieccanhan;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class ThongBaoReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("taskTitle");
        String desc = intent.getStringExtra("taskDesc");
        int notificationId = (int) System.currentTimeMillis();

        Intent fullScreenIntent = new Intent(context, BaoThucActivity.class);
        fullScreenIntent.putExtra("taskTitle", title);
        fullScreenIntent.putExtra("taskDesc", desc);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, notificationId,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ Sắp đến hạn: " + title)
                .setContentText("Chạm để mở báo thức")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setOngoing(true)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }

        try {
            context.startActivity(fullScreenIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}