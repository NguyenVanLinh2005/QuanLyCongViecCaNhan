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
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AiPlannerActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;

    private EditText etAiGoalInput;
    private ChipGroup chipGroupTimeframe;
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
        chipGroupTimeframe = findViewById(R.id.chipGroupTimeframe);
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

        int selectedChipId = chipGroupTimeframe.getCheckedChipId();
        String timeframeText = "1 Tuần";
        int daysOffset = 7;

        if (selectedChipId == R.id.chip1Day) {
            timeframeText = "1 Ngày";
            daysOffset = 1;
        } else if (selectedChipId == R.id.chip1Month) {
            timeframeText = "1 Tháng";
            daysOffset = 30;
        }

        int strategyIndex = spinnerAiStrategy.getSelectedItemPosition();
        String selectedStrategy = strategies[strategyIndex];

        // Lời khuyên & Giải thích chiến lược từ AI
        StringBuilder explanationSb = new StringBuilder();
        explanationSb.append("• Mục tiêu: ").append(goal).append("\n");
        explanationSb.append("• Khung thời gian: ").append(timeframeText).append("\n");
        explanationSb.append("• Chiến lược AI áp dụng: ").append(selectedStrategy).append("\n\n");

        if (strategyIndex == 0) { // Eisenhower
            explanationSb.append("💡 AI đã chia mục tiêu của bạn thành 4 nhóm theo Ma Trận Eisenhower (Khẩn cấp/Quan trọng) để tập trung giải quyết các việc ưu tiên cao trước.");
        } else if (strategyIndex == 1) { // Pomodoro & Time Blocking
            explanationSb.append("💡 AI áp dụng kỹ thuật Time-Blocking chia khung giờ tập trung (25 phút Pomodoro) để tránh xao nhãng và hoàn thành từng chặng.");
        } else if (strategyIndex == 2) { // SMART
            explanationSb.append("💡 AI quy hoạch mục tiêu SMART (Đo lường được, Khả thi, Thời hạn rõ ràng) giúp bạn dễ dàng theo dõi chặng đường hoàn thành.");
        } else { // Balanced
            explanationSb.append("💡 AI phân bổ chẵn giữa Công việc, Học tập và Nghỉ ngơi/Sức khỏe giúp bạn duy trì năng suất bền vững.");
        }

        tvAiStrategyExplanation.setText(explanationSb.toString());
        cardAiInsights.setVisibility(View.VISIBLE);

        // Sinh danh sách công việc đề xuất
        generatedProposedTasks.clear();
        layoutAiProposedTasksList.removeAllViews();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        if (daysOffset == 1) { // 1 Ngày
            cal.add(Calendar.HOUR_OF_DAY, 2);
            generatedProposedTasks.add(new Task(
                    "Buổi sáng: Khởi động & " + goal,
                    "Tập trung giải quyết các nội dung quan trọng nhất.",
                    false, 2, sdf.format(cal.getTime()), "Công việc", true, "Tại nhà / Phòng làm việc", "Không lặp", "Chuẩn bị tài liệu, Lập checklist"
            ));

            cal.add(Calendar.HOUR_OF_DAY, 4);
            generatedProposedTasks.add(new Task(
                    "Buổi chiều: Thực hành & Hoàn thiện " + goal,
                    "Giải quyết phần bài tập và rà soát lỗi.",
                    false, 1, sdf.format(cal.getTime()), "Học tập", false, "Phòng học", "Không lặp", "Thực hành, Đánh giá kết quả"
            ));

            cal.add(Calendar.HOUR_OF_DAY, 3);
            generatedProposedTasks.add(new Task(
                    "Buổi tối: Tổng kết & Vận động nhẹ",
                    "Nghỉ ngơi và xem lại tiến độ trong ngày.",
                    false, 0, sdf.format(cal.getTime()), "Sức khỏe", false, "Tại nhà", "Không lặp", "Nghỉ ngơi 30p, Đọc sách"
            ));

        } else if (daysOffset == 7) { // 1 Tuần
            cal.add(Calendar.DAY_OF_MONTH, 2);
            generatedProposedTasks.add(new Task(
                    "Chặng 1 (Ngày 1-2): Thu thập thông tin & " + goal,
                    "Nghiên cứu tài liệu và xây dựng khung kế hoạch.",
                    false, 2, sdf.format(cal.getTime()), "Học tập", true, "Thư viện / Phòng học", "Không lặp", "Đọc tài liệu, Lập đề cương"
            ));

            cal.add(Calendar.DAY_OF_MONTH, 3);
            generatedProposedTasks.add(new Task(
                    "Chặng 2 (Ngày 3-5): Thực thi cốt lõi & " + goal,
                    "Tập trung hoàn thành phần khối lượng lớn nhất.",
                    false, 2, sdf.format(cal.getTime()), "Công việc", true, "Phòng làm việc", "Không lặp", "Soạn thảo, Chạy kiểm thử"
            ));

            cal.add(Calendar.DAY_OF_MONTH, 2);
            generatedProposedTasks.add(new Task(
                    "Chặng 3 (Ngày 6-7): Rà soát, Tổng kết & " + goal,
                    "Kiểm tra lại toàn bộ kết quả tuần.",
                    false, 1, sdf.format(cal.getTime()), "Cá nhân", false, "Tại nhà", "Không lặp", "Tổng kết tuần, Đánh giá"
            ));

        } else { // 1 Tháng
            cal.add(Calendar.DAY_OF_MONTH, 7);
            generatedProposedTasks.add(new Task(
                    "Tuần 1: Khởi động & Xây dựng nền tảng " + goal,
                    "Hoàn thành các kiến thức và chuẩn bị ban đầu.",
                    false, 2, sdf.format(cal.getTime()), "Học tập", true, "Trường / Cơ quan", "Hằng tuần", "Lập mục tiêu tuần 1, Tìm tài liệu"
            ));

            cal.add(Calendar.DAY_OF_MONTH, 7);
            generatedProposedTasks.add(new Task(
                    "Tuần 2: Tăng tốc triển khai " + goal,
                    "Thực hiện 50% khối lượng mục tiêu tháng.",
                    false, 2, sdf.format(cal.getTime()), "Công việc", true, "Phòng làm việc", "Hằng tuần", "Đánh giá giữa chặng"
            ));

            cal.add(Calendar.DAY_OF_MONTH, 7);
            generatedProposedTasks.add(new Task(
                    "Tuần 3: Đạt ngưỡng mục tiêu & Kiểm tra " + goal,
                    "Hoàn thành các hạng mục còn lại.",
                    false, 1, sdf.format(cal.getTime()), "Cá nhân", false, "Tại nhà", "Hằng tuần", "Rà soát chi tiết"
            ));

            cal.add(Calendar.DAY_OF_MONTH, 7);
            generatedProposedTasks.add(new Task(
                    "Tuần 4: Tổng kết & Đánh giá kết quả " + goal,
                    "Nghiệm thu toàn bộ mục tiêu trong tháng.",
                    false, 1, sdf.format(cal.getTime()), "Tài chính", true, "Tại nhà", "Hằng tháng", "Viết báo cáo tổng kết"
            ));
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
        Toast.makeText(this, "AI đã phân tích và tạo lịch công việc thành công!", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(this, "Đã lưu thành công " + generatedProposedTasks.size() + " công việc AI vào CSDL SQLite!", Toast.LENGTH_LONG).show();
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