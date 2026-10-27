package com.example.quanlycongvieccanhan;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.viewmodel.TaskViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ChiTietActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private int taskId = -1;
    private Task currentTask = null;

    private CheckBox cbDetailComplete;
    private TextView tvDetailTitle, tvDetailCategory, tvDetailPriority, tvDetailDesc, tvDetailLocation, tvDetailDateTime, tvDetailRepeat;
    private ImageView imgDetailFavorite;
    private MaterialCardView cardSubTasks;
    private LinearLayout layoutSubTasksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        taskId = getIntent().getIntExtra("taskId", -1);
        if (taskId == -1) {
            finish();
            return;
        }

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarDetail);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        cbDetailComplete = findViewById(R.id.cbDetailComplete);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailPriority = findViewById(R.id.tvDetailPriority);
        tvDetailDesc = findViewById(R.id.tvDetailDesc);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailDateTime = findViewById(R.id.tvDetailDateTime);
        tvDetailRepeat = findViewById(R.id.tvDetailRepeat);
        imgDetailFavorite = findViewById(R.id.imgDetailFavorite);

        cardSubTasks = findViewById(R.id.cardSubTasks);
        layoutSubTasksList = findViewById(R.id.layoutSubTasksList);

        MaterialButton btnEditDetail = findViewById(R.id.btnEditDetail);
        MaterialButton btnShareDetail = findViewById(R.id.btnShareDetail);
        MaterialButton btnDeleteDetail = findViewById(R.id.btnDeleteDetail);

        taskViewModel.getAllTasks().observe(this, tasks -> {
            for (Task t : tasks) {
                if (t.getId() == taskId) {
                    currentTask = t;
                    displayTaskDetails(t);
                    break;
                }
            }
        });

        cbDetailComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentTask != null) {
                currentTask.setCompleted(isChecked);
                taskViewModel.update(currentTask);
            }
        });

        btnEditDetail.setOnClickListener(v -> {
            Intent intent = new Intent(this, ThemCongViecActivity.class);
            intent.putExtra("taskId", taskId);
            startActivity(intent);
        });

        btnShareDetail.setOnClickListener(v -> {
            if (currentTask == null) return;
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String shareText = "📋 Công việc: " + currentTask.getTitle() +
                    "\n• Mô tả: " + currentTask.getDescription() +
                    "\n• Danh mục: " + currentTask.getCategory() +
                    "\n• Trạng thái: " + (currentTask.isCompleted() ? "✅ Đã xong" : "⏳ Đang chờ");
            if (currentTask.getDateTime() != null && !currentTask.getDateTime().isEmpty()) {
                shareText += "\nHạn chót: " + currentTask.getDateTime();
            }
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Chia sẻ công việc qua..."));
        });

        btnDeleteDetail.setOnClickListener(v -> {
            if (currentTask == null) return;
            new AlertDialog.Builder(this)
                    .setTitle("Xóa công việc")
                    .setMessage("Bạn có chắc chắn muốn xóa công việc này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        taskViewModel.delete(currentTask);
                        Toast.makeText(this, "Đã xóa công việc", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void displayTaskDetails(Task t) {
        tvDetailTitle.setText(t.getTitle());
        tvDetailDesc.setText(t.getDescription() != null && !t.getDescription().isEmpty() ? t.getDescription() : "Không có mô tả chi tiết.");

        cbDetailComplete.setOnCheckedChangeListener(null);
        cbDetailComplete.setChecked(t.isCompleted());
        if (t.isCompleted()) {
            tvDetailTitle.setPaintFlags(tvDetailTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvDetailTitle.setPaintFlags(tvDetailTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        cbDetailComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            t.setCompleted(isChecked);
            taskViewModel.update(t);
        });

        // Danh mục
        String cat = t.getCategory();
        if (cat == null || cat.isEmpty()) cat = "Cá nhân";
        tvDetailCategory.setText("🏷️ " + cat);

        // Mức độ ưu tiên
        if (t.getPriority() == 2) {
            tvDetailPriority.setText("🔴 Ưu tiên: Cao");
            tvDetailPriority.setTextColor(Color.parseColor("#EE5D50"));
        } else if (t.getPriority() == 1) {
            tvDetailPriority.setText("🟡 Ưu tiên: Vừa");
            tvDetailPriority.setTextColor(Color.parseColor("#D99B00"));
        } else {
            tvDetailPriority.setText("🟢 Ưu tiên: Thấp");
            tvDetailPriority.setTextColor(Color.parseColor("#05CD99"));
        }

        // Yêu thích
        if (t.isFavorite()) {
            imgDetailFavorite.setVisibility(View.VISIBLE);
            imgDetailFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            imgDetailFavorite.setVisibility(View.GONE);
        }

        // Địa điểm
        if (t.getLocation() != null && !t.getLocation().isEmpty()) {
            tvDetailLocation.setVisibility(View.VISIBLE);
            tvDetailLocation.setText("📍 Địa điểm: " + t.getLocation());
        } else {
            tvDetailLocation.setVisibility(View.GONE);
        }

        // Hạn chót
        if (t.getDateTime() != null && !t.getDateTime().isEmpty()) {
            tvDetailDateTime.setVisibility(View.VISIBLE);
            tvDetailDateTime.setText("🕒 Hạn chót: " + t.getDateTime());
        } else {
            tvDetailDateTime.setVisibility(View.GONE);
        }

        // Lặp lại
        if (t.getRepeatType() != null && !t.getRepeatType().isEmpty() && !"Không lặp".equalsIgnoreCase(t.getRepeatType())) {
            tvDetailRepeat.setVisibility(View.VISIBLE);
            tvDetailRepeat.setText("🔄 Lặp lại: " + t.getRepeatType());
        } else {
            tvDetailRepeat.setVisibility(View.GONE);
        }

        // Việc con (Checklist)
        layoutSubTasksList.removeAllViews();
        if (t.getSubTasks() != null && !t.getSubTasks().isEmpty()) {
            cardSubTasks.setVisibility(View.VISIBLE);
            String[] subItems = t.getSubTasks().split(",");
            for (String sub : subItems) {
                String trimmed = sub.trim();
                if (!trimmed.isEmpty()) {
                    CheckBox cbSub = new CheckBox(this);
                    cbSub.setText(trimmed);
                    cbSub.setTextSize(14);
                    cbSub.setTextColor(Color.parseColor("#2B3674"));
                    layoutSubTasksList.addView(cbSub);
                }
            }
        } else {
            cardSubTasks.setVisibility(View.GONE);
        }
    }
}