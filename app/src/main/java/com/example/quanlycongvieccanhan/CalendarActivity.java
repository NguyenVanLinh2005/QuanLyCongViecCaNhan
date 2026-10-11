package com.example.quanlycongvieccanhan;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.viewmodel.TaskViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private TaskAdapter adapter;
    private TextView tvSelectedDateTitle;
    private View layoutCalendarEmpty;

    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;
    private List<Task> allTaskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarCalendar);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSelectedDateTitle = findViewById(R.id.tvSelectedDateTitle);
        layoutCalendarEmpty = findViewById(R.id.layoutCalendarEmpty);
        CalendarView calendarView = findViewById(R.id.calendarView);

        RecyclerView recyclerView = findViewById(R.id.rvCalendarTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(new TaskAdapter.OnTaskItemClickListener() {
            @Override
            public void onDeleteClick(Task task) { taskViewModel.delete(task); }
            @Override
            public void onStatusChange(Task task, boolean isCompleted) {
                task.setCompleted(isCompleted);
                taskViewModel.update(task);
            }
            @Override
            public void onEditClick(Task task) {}
            @Override
            public void onFavoriteClick(Task task, boolean isFavorite) {
                task.setFavorite(isFavorite);
                taskViewModel.update(task);
            }
            @Override
            public void onLongClick(Task task) {}
        });
        recyclerView.setAdapter(adapter);

        // Khởi tạo ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(this, tasks -> {
            allTaskList = tasks;
            filterTasksForSelectedDate();
        });

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = dayOfMonth;
            filterTasksForSelectedDate();
        });
    }

    private void filterTasksForSelectedDate() {
        String dateStrPattern1 = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
        String dateStrPattern2 = String.format(Locale.getDefault(), "%d/%d/%d", selectedDay, selectedMonth + 1, selectedYear);

        tvSelectedDateTitle.setText("📅 Công việc ngày: " + dateStrPattern1);

        List<Task> filtered = new ArrayList<>();
        for (Task t : allTaskList) {
            if (t.getDateTime() != null && (t.getDateTime().contains(dateStrPattern1) || t.getDateTime().contains(dateStrPattern2))) {
                filtered.add(t);
            }
        }

        adapter.setTasks(filtered);
        if (filtered.isEmpty()) {
            layoutCalendarEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutCalendarEmpty.setVisibility(View.GONE);
        }
    }
}