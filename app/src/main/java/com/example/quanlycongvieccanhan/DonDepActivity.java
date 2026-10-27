package com.example.quanlycongvieccanhan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.viewmodel.TaskViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DonDepActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private CongViecAdapter adapter;
    private TextView tvCompletedCount;
    private View layoutCleanEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean_completed);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarClean);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        layoutCleanEmpty = findViewById(R.id.layoutCleanEmpty);
        MaterialButton btnDeleteAllCompleted = findViewById(R.id.btnDeleteAllCompleted);

        RecyclerView recyclerView = findViewById(R.id.rvCompletedTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CongViecAdapter(new CongViecAdapter.OnTaskItemClickListener() {
            @Override
            public void onDeleteClick(Task task) { taskViewModel.delete(task); }
            @Override
            public void onStatusChange(Task task, boolean isCompleted) {
                task.setCompleted(isCompleted);
                taskViewModel.update(task);
            }
            @Override
            public void onEditClick(Task task) {
                Intent intent = new Intent(DonDepActivity.this, ChiTietActivity.class);
                intent.putExtra("taskId", task.getId());
                startActivity(intent);
            }
            @Override
            public void onFavoriteClick(Task task, boolean isFavorite) {
                task.setFavorite(isFavorite);
                taskViewModel.update(task);
            }
            @Override
            public void onLongClick(Task task) {}
        });
        recyclerView.setAdapter(adapter);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(this, tasks -> {
            List<Task> completedList = new ArrayList<>();
            for (Task t : tasks) {
                if (t.isCompleted()) completedList.add(t);
            }

            tvCompletedCount.setText("Tổng số công việc đã xong: " + completedList.size());
            adapter.setTasks(completedList);

            if (completedList.isEmpty()) {
                layoutCleanEmpty.setVisibility(View.VISIBLE);
            } else {
                layoutCleanEmpty.setVisibility(View.GONE);
            }
        });

        btnDeleteAllCompleted.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa tất cả công việc hoàn thành")
                    .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn tất cả công việc đã hoàn thành không?")
                    .setPositiveButton("XÓA", (dialog, which) -> {
                        taskViewModel.deleteCompletedTasks();
                        Toast.makeText(this, "Đã dọn dẹp tất cả các việc hoàn thành!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
}