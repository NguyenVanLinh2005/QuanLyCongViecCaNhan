package com.example.quanlycongvieccanhan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.viewmodel.TaskViewModel;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class DanhMucActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private CongViecAdapter adapter;
    private TextView tvCatSummary;
    private View layoutCategoryEmpty;
    private String selectedCategory = "Cá nhân";
    private List<Task> allTaskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarCategory);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCatSummary = findViewById(R.id.tvCatSummary);
        layoutCategoryEmpty = findViewById(R.id.layoutCategoryEmpty);

        RecyclerView recyclerView = findViewById(R.id.rvCategoryTasks);
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
                Intent intent = new Intent(DanhMucActivity.this, ChiTietActivity.class);
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

        ChipGroup chipGroupCategories = findViewById(R.id.chipGroupCategories);
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipCatWork) selectedCategory = "Công việc";
            else if (checkedId == R.id.chipCatStudy) selectedCategory = "Học tập";
            else if (checkedId == R.id.chipCatHealth) selectedCategory = "Sức khỏe";
            else if (checkedId == R.id.chipCatFinance) selectedCategory = "Tài chính";
            else selectedCategory = "Cá nhân";

            filterTasksByCategory();
        });

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(this, tasks -> {
            allTaskList = tasks;
            filterTasksByCategory();
        });
    }

    private void filterTasksByCategory() {
        List<Task> filtered = new ArrayList<>();
        for (Task t : allTaskList) {
            if (selectedCategory.equalsIgnoreCase(t.getCategory())) {
                filtered.add(t);
            }
        }

        tvCatSummary.setText("Có " + filtered.size() + " công việc thuộc danh mục: " + selectedCategory);
        adapter.setTasks(filtered);

        if (filtered.isEmpty()) {
            layoutCategoryEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutCategoryEmpty.setVisibility(View.GONE);
        }
    }
}