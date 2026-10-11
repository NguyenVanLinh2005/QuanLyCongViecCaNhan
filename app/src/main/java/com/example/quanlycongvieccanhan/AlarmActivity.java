package com.example.quanlycongvieccanhan;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer; // Sử dụng MediaPlayer thay cho Ringtone

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Đánh thức điện thoại và GIỮ MÀN HÌNH SÁNG LIÊN TỤC
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_alarm);

        String title = getIntent().getStringExtra("taskTitle");
        String desc = getIntent().getStringExtra("taskDesc");

        TextView tvTitle = findViewById(R.id.tvAlarmTitle);
        TextView tvDesc = findViewById(R.id.tvAlarmDesc);
        tvTitle.setText(title);
        tvDesc.setText(desc);

        // Hiệu ứng chuông rung lắc
        ImageView imgBell = findViewById(R.id.imgBell);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f);
        PropertyValuesHolder rotate = PropertyValuesHolder.ofFloat("rotation", 0f, -20f, 20f, 0f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(imgBell, scaleX, scaleY, rotate);
        animator.setDuration(600);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.start();

        // BẬT NHẠC CHUÔNG LẶP LẠI VÔ HẠN
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, alarmUri);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());

            // Dòng lệnh then chốt: Bắt buộc kêu liên tục cho đến khi bị tắt
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tắt báo thức khi chủ động nhấn nút
        Button btnDismiss = findViewById(R.id.btnDismiss);
        btnDismiss.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đảm bảo nhạc tắt triệt để khi hộp thoại bị đóng bằng phím Back
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}