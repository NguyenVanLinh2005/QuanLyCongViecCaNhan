package com.example.quanlycongvieccanhan;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.viewmodel.TaskViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BackupRestoreActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private TextView tvTotalBackupTasks;
    private EditText etBackupPreview;
    private String generatedReportText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarBackup);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTotalBackupTasks = findViewById(R.id.tvTotalBackupTasks);
        etBackupPreview = findViewById(R.id.etBackupPreview);

        MaterialButton btnShareBackup = findViewById(R.id.btnShareBackup);
        MaterialButton btnCopyBackup = findViewById(R.id.btnCopyBackup);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(this, tasks -> {
            int count = (tasks != null) ? tasks.size() : 0;
            tvTotalBackupTasks.setText("Số công việc hiện tại: " + count);
            generatedReportText = buildReportString(tasks);
            etBackupPreview.setText(generatedReportText);
        });

        btnShareBackup.setOnClickListener(v -> {
            if (generatedReportText.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu để chia sẻ", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, generatedReportText);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Sao lưu & Chia sẻ dữ liệu"));
        });

        btnCopyBackup.setOnClickListener(v -> {
            if (generatedReportText.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu để sao chép", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("BackupTasks", generatedReportText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Đã sao chép báo cáo vào bộ nhớ tạm!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildReportString(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return "Chưa có dữ liệu công việc nào trong ứng dụng.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 BÁO CÁO SAO LƯU DỰ ÁN QUẢN LÝ CÔNG VIỆC CÁ NHÂN\n");
        sb.append("===============================================\n\n");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append((i + 1)).append(". ").append(t.isFavorite() ? "⭐ " : "").append(t.getTitle()).append("\n");
            if (t.getDescription() != null && !t.getDescription().isEmpty()) {
                sb.append("   - Mô tả: ").append(t.getDescription()).append("\n");
            }
            if (t.getLocation() != null && !t.getLocation().isEmpty()) {
                sb.append("   - Địa điểm: ").append(t.getLocation()).append("\n");
            }
            if (t.getSubTasks() != null && !t.getSubTasks().isEmpty()) {
                sb.append("   - Việc con: ").append(t.getSubTasks()).append("\n");
            }
            sb.append("   - Danh mục: ").append(t.getCategory()).append("\n");
            sb.append("   - Mức ưu tiên: ").append(t.getPriority() == 2 ? "Cao" : (t.getPriority() == 1 ? "Vừa" : "Thấp")).append("\n");
            sb.append("   - Trạng thái: ").append(t.isCompleted() ? "✅ Đã xong" : "⏳ Đang chờ").append("\n");
            if (t.getDateTime() != null && !t.getDateTime().isEmpty()) {
                sb.append("   - Hạn chót: ").append(t.getDateTime()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}