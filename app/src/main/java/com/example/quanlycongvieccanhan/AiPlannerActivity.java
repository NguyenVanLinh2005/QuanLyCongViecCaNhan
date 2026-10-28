package com.example.quanlycongvieccanhan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.viewmodel.TaskViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AiPlannerActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;

    private EditText etAiGoalInput;
    private EditText etAiDaysInput;
    private Spinner spinnerAiStrategy;

    private MaterialCardView cardAiInsights;
    private TextView tvAiStrategyExplanation;

    private MaterialCardView cardAiProposedTasks;
    private LinearLayout layoutAiProposedTasksList;

    private final List<Task> generatedProposedTasks = new ArrayList<>();

    private final String[] strategies = {
            "⚡ Phương pháp Eisenhower (Quan trọng & Khẩn cấp)",
            "⏱️ Phương pháp Pomodoro & Time Blocking (Khung giờ tập trung)",
            "🎯 Phương pháp SMART Goals (Mục tiêu đo lường cụ thể)",
            "🧘 Phương pháp Cân Bằng (Công việc & Sức khỏe cá nhân)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_planner);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarAi);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        etAiGoalInput = findViewById(R.id.etAiGoalInput);
        etAiDaysInput = findViewById(R.id.etAiDaysInput);
        spinnerAiStrategy = findViewById(R.id.spinnerAiStrategy);

        cardAiInsights = findViewById(R.id.cardAiInsights);
        tvAiStrategyExplanation = findViewById(R.id.tvAiStrategyExplanation);

        cardAiProposedTasks = findViewById(R.id.cardAiProposedTasks);
        layoutAiProposedTasksList = findViewById(R.id.layoutAiProposedTasksList);

        MaterialButton btnGenerateAiPlan = findViewById(R.id.btnGenerateAiPlan);
        MaterialButton btnApplyAiSchedule = findViewById(R.id.btnApplyAiSchedule);

        ArrayAdapter<String> strategyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, strategies);
        spinnerAiStrategy.setAdapter(strategyAdapter);

        btnGenerateAiPlan.setOnClickListener(v -> generateAiPlan());
        btnApplyAiSchedule.setOnClickListener(v -> applyAiPlanToDatabase());
    }

    private void generateAiPlan() {
        String goal = etAiGoalInput.getText().toString().trim();
        if (goal.isEmpty()) {
            goal = "Phát triển công việc & Mục tiêu cá nhân";
        }

        int numDays = 7;
        try {
            String daysStr = etAiDaysInput.getText().toString().trim();
            if (!daysStr.isEmpty()) {
                numDays = Integer.parseInt(daysStr);
            }
        } catch (Exception e) {
            numDays = 7;
        }

        if (numDays < 1) numDays = 1;
        if (numDays > 60) numDays = 60; // Giới hạn tối đa 60 ngày

        int strategyIndex = spinnerAiStrategy.getSelectedItemPosition();
        String selectedStrategy = strategies[strategyIndex];

        // Lời khuyên & Giải thích chiến lược từ AI
        StringBuilder explanationSb = new StringBuilder();
        explanationSb.append("• Mục tiêu: ").append(goal).append("\n");
        explanationSb.append("• Số ngày AI lên lịch: ").append(numDays).append(" ngày\n");
        explanationSb.append("• Chiến lược AI áp dụng: ").append(selectedStrategy).append("\n\n");

        if (strategyIndex == 0) {
            explanationSb.append("💡 AI đã chia nhỏ công việc cho từng ngày theo Ma Trận Eisenhower để hoàn thành các phần ưu tiên cao trước.");
        } else if (strategyIndex == 1) {
            explanationSb.append("💡 AI áp dụng kỹ thuật Time-Blocking chia khung giờ tập trung hằng ngày giúp bạn duy trì nhịp độ ổn định.");
        } else if (strategyIndex == 2) {
            explanationSb.append("💡 AI quy hoạch mục tiêu SMART cho từng ngày cụ thể, giúp bạn đo lường tiến độ rõ ràng.");
        } else {
            explanationSb.append("💡 AI phân bổ khối lượng làm việc chẵn giữa Công việc, Học tập và Sức khỏe cho từng ngày.");
        }

        tvAiStrategyExplanation.setText(explanationSb.toString());
        cardAiInsights.setVisibility(View.VISIBLE);

        // Sinh danh sách công việc đề xuất cho TỪNG NGÀY
        generatedProposedTasks.clear();
        layoutAiProposedTasksList.removeAllViews();

        SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy 20:00", Locale.getDefault());
        SimpleDateFormat sdfShort = new SimpleDateFormat("dd/MM", Locale.getDefault());

        String[] categoriesList = {"Học tập", "Công việc", "Cá nhân", "Sức khỏe", "Tài chính"};

        for (int i = 1; i <= numDays; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, i - 1);
            Date taskDate = cal.getTime();

            String dateShort = sdfShort.format(taskDate);
            String fullDateTime = sdfFull.format(taskDate);

            String taskTitle;
            String taskDesc;
            int priority;
            String category = categoriesList[(i - 1) % categoriesList.length];

            if (i == 1) {
                taskTitle = "Ngày 1 (" + dateShort + "): Khởi động & Lập kế hoạch " + goal;
                taskDesc = "Chuẩn bị tài liệu, định hình các bước triển khai.";
                priority = 2;
            } else if (i == numDays) {
                taskTitle = "Ngày " + i + " (" + dateShort + "): Tổng kết & Đánh giá kết quả " + goal;
                taskDesc = "Nghiệm thu toàn bộ công việc và rút kinh nghiệm.";
                priority = 1;
            } else if (i % 2 == 0) {
                taskTitle = "Ngày " + i + " (" + dateShort + "): Thực thi trọng tâm " + goal;
                taskDesc = "Tập trung giải quyết các mục tiêu chính trong ngày.";
                priority = 2;
            } else {
                taskTitle = "Ngày " + i + " (" + dateShort + "): Củng cố & Mở rộng " + goal;
                taskDesc = "Thực hành, rà soát và chuẩn bị cho các bước tiếp theo.";
                priority = 1;
            }

            Task dailyTask = new Task(
                    taskTitle,
                    taskDesc,
                    false,
                    priority,
                    fullDateTime,
                    category,
                    (i == 1 || i == numDays),
                    "Tại nhà / Phòng làm việc",
                    "Không lặp",
                    "Chuẩn bị nội dung, Thực hành ngày " + i
            );

            generatedProposedTasks.add(dailyTask);
        }

        // Hiển thị công việc đề xuất lên giao diện
        for (int i = 0; i < generatedProposedTasks.size(); i++) {
            Task t = generatedProposedTasks.get(i);
            MaterialCardView card = new MaterialCardView(this);
            card.setRadius(24);
            card.setCardElevation(0);
            card.setStrokeColor(Color.parseColor("#E2E8F0"));
            card.setStrokeWidth(2);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 16);
            card.setLayoutParams(lp);

            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(24, 20, 24, 20);

            TextView tvTitle = new TextView(this);
            tvTitle.setText("📌 " + t.getTitle());
            tvTitle.setTextSize(15);
            tvTitle.setTextColor(Color.parseColor("#2B3674"));
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            container.addView(tvTitle);

            TextView tvDesc = new TextView(this);
            tvDesc.setText("• " + t.getDescription() + " (Hạn chót: " + t.getDateTime() + ")");
            tvDesc.setTextSize(13);
            tvDesc.setTextColor(Color.parseColor("#A3AED0"));
            tvDesc.setPadding(0, 6, 0, 0);
            container.addView(tvDesc);

            card.addView(container);
            layoutAiProposedTasksList.addView(card);
        }

        cardAiProposedTasks.setVisibility(View.VISIBLE);
        Toast.makeText(this, "AI đã phân tích và tạo lịch công việc chi tiết cho " + numDays + " ngày!", Toast.LENGTH_SHORT).show();
    }

    private void applyAiPlanToDatabase() {
        if (generatedProposedTasks.isEmpty()) {
            Toast.makeText(this, "Không có lịch công việc AI nào để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Task t : generatedProposedTasks) {
            taskViewModel.insert(t);
            scheduleNotification(t);
        }

        Toast.makeText(this, "Đã lưu thành công " + generatedProposedTasks.size() + " công việc AI cho từng ngày vào CSDL SQLite!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void scheduleNotification(Task task) {
        if (task.getDateTime() == null || task.getDateTime().isEmpty()) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(task.getDateTime());
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MINUTE, -5);

                long alarmTime = calendar.getTimeInMillis();
                if (alarmTime > System.currentTimeMillis()) {
                    Intent intent = new Intent(this, ThongBaoReceiver.class);
                    intent.putExtra("taskTitle", task.getTitle());
                    intent.putExtra("taskDesc", task.getDescription());

                    int requestCode = (task.getTitle() + task.getDateTime()).hashCode();
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            this, requestCode, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                            } else {
                                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                            }
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}