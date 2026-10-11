package com.example.quanlycongvieccanhan;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.viewmodel.TaskViewModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class ThongKeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarStats);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        CircularProgressIndicator circularProgress = findViewById(R.id.circularProgress);
        TextView tvPercentText = findViewById(R.id.tvPercentText);
        TextView tvProgressSubtitle = findViewById(R.id.tvProgressSubtitle);

        TextView tvTotalTasksCount = findViewById(R.id.tvTotalTasksCount);
        TextView tvCompletedTasksCount = findViewById(R.id.tvCompletedTasksCount);
        TextView tvPendingTasksCount = findViewById(R.id.tvPendingTasksCount);

        TextView tvFavoriteTasks = findViewById(R.id.tvFavoriteTasks);
        TextView tvHighPriority = findViewById(R.id.tvHighPriority);
        TextView tvMedPriority = findViewById(R.id.tvMedPriority);
        TextView tvLowPriority = findViewById(R.id.tvLowPriority);

        TextView tvCatWork = findViewById(R.id.tvCatWork);
        TextView tvCatStudy = findViewById(R.id.tvCatStudy);
        TextView tvCatPersonal = findViewById(R.id.tvCatPersonal);
        TextView tvCatHealth = findViewById(R.id.tvCatHealth);
        TextView tvCatFinance = findViewById(R.id.tvCatFinance);

        TaskViewModel taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(this, tasks -> {
            int total = tasks.size();
            int completed = 0;
            int favoriteCount = 0;
            int highPriorityCount = 0;
            int medPriorityCount = 0;
            int lowPriorityCount = 0;

            int workCount = 0;
            int studyCount = 0;
            int personalCount = 0;
            int healthCount = 0;
            int financeCount = 0;

            for (Task t : tasks) {
                if (t.isCompleted()) completed++;
                if (t.isFavorite()) favoriteCount++;

                if (t.getPriority() == 2) highPriorityCount++;
                else if (t.getPriority() == 1) medPriorityCount++;
                else lowPriorityCount++;

                String cat = t.getCategory();
                if ("Công việc".equalsIgnoreCase(cat)) workCount++;
                else if ("Học tập".equalsIgnoreCase(cat)) studyCount++;
                else if ("Sức khỏe".equalsIgnoreCase(cat)) healthCount++;
                else if ("Tài chính".equalsIgnoreCase(cat)) financeCount++;
                else personalCount++;
            }

            int pending = total - completed;
            int percent = (total > 0) ? (completed * 100 / total) : 0;

            circularProgress.setProgress(percent);
            tvPercentText.setText(percent + "%");
            tvProgressSubtitle.setText("Đã hoàn thành " + completed + "/" + total + " công việc");

            tvTotalTasksCount.setText(String.valueOf(total));
            tvCompletedTasksCount.setText(String.valueOf(completed));
            tvPendingTasksCount.setText(String.valueOf(pending));

            tvFavoriteTasks.setText("⭐ Việc quan trọng / Yêu thích: " + favoriteCount);
            tvHighPriority.setText("🔴 Ưu tiên Cao: " + highPriorityCount);
            tvMedPriority.setText("🟡 Ưu tiên Vừa: " + medPriorityCount);
            tvLowPriority.setText("🟢 Ưu tiên Thấp: " + lowPriorityCount);

            tvCatWork.setText("💼 Công việc: " + workCount);
            tvCatStudy.setText("📚 Học tập: " + studyCount);
            tvCatPersonal.setText("👤 Cá nhân: " + personalCount);
            tvCatHealth.setText("🏥 Sức khỏe: " + healthCount);
            tvCatFinance.setText("💰 Tài chính: " + financeCount);
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }
}