package com.example.quanlycongvieccanhan;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.viewmodel.TaskViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ThemCongViecActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private EditText etTitle, etDescription, etLocation, etSubTasks;
    private Spinner spinnerCategory, spinnerRepeat;
    private RadioGroup rgPriority;
    private RadioButton rbLow, rbMed, rbHigh;
    private CheckBox cbFavorite;
    private TextView tvDateTime;

    private String selectedDateTimeStr = "";
    private int taskToEditId = -1;
    private Task existingTaskToEdit = null;

    private final String[] categories = {"Cá nhân", "Công việc", "Học tập", "Sức khỏe", "Tài chính"};
    private final String[] repeatOptions = {"Không lặp", "Hằng ngày", "Hằng tuần", "Hằng tháng"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarAddTask);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etSubTasks = findViewById(R.id.etSubTasks);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerRepeat = findViewById(R.id.spinnerRepeat);

        rgPriority = findViewById(R.id.rgPriority);
        rbLow = findViewById(R.id.rbLow);
        rbMed = findViewById(R.id.rbMed);
        rbHigh = findViewById(R.id.rbHigh);

        cbFavorite = findViewById(R.id.cbFavorite);
        tvDateTime = findViewById(R.id.tvDateTime);
        MaterialButton btnSaveTask = findViewById(R.id.btnSaveTask);

        // Setup Spinners
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, repeatOptions);
        spinnerRepeat.setAdapter(repeatAdapter);

        // Date/Time Picker
        tvDateTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                    selectedDateTimeStr = String.format(Locale.getDefault(), "%02d/%02d/%d %02d:%02d", dayOfMonth, month + 1, year, hourOfDay, minute);
                    tvDateTime.setText("🕒 " + selectedDateTimeStr);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Kiểm tra nếu là chỉnh sửa
        taskToEditId = getIntent().getIntExtra("taskId", -1);
        if (taskToEditId != -1) {
            toolbar.setTitle("Chỉnh Sửa Công Việc");
            taskViewModel.getAllTasks().observe(this, tasks -> {
                for (Task t : tasks) {
                    if (t.getId() == taskToEditId) {
                        existingTaskToEdit = t;
                        populateTaskFields(t);
                        break;
                    }
                }
            });
        }

        btnSaveTask.setOnClickListener(v -> saveTaskData());
    }

    private void populateTaskFields(Task t) {
        etTitle.setText(t.getTitle());
        etDescription.setText(t.getDescription());
        if (t.getLocation() != null) etLocation.setText(t.getLocation());
        if (t.getSubTasks() != null) etSubTasks.setText(t.getSubTasks());

        if (t.getCategory() != null) {
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equalsIgnoreCase(t.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }

        if (t.getRepeatType() != null) {
            for (int i = 0; i < repeatOptions.length; i++) {
                if (repeatOptions[i].equalsIgnoreCase(t.getRepeatType())) {
                    spinnerRepeat.setSelection(i);
                    break;
                }
            }
        }

        if (t.getPriority() == 2) rbHigh.setChecked(true);
        else if (t.getPriority() == 1) rbMed.setChecked(true);
        else rbLow.setChecked(true);

        cbFavorite.setChecked(t.isFavorite());

        if (t.getDateTime() != null && !t.getDateTime().isEmpty()) {
            selectedDateTimeStr = t.getDateTime();
            tvDateTime.setText("🕒 " + selectedDateTimeStr);
        }
    }

    private void saveTaskData() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String loc = etLocation.getText().toString().trim();
        String sub = etSubTasks.getText().toString().trim();
        String selectedCategory = categories[spinnerCategory.getSelectedItemPosition()];
        String selectedRepeat = repeatOptions[spinnerRepeat.getSelectedItemPosition()];
        boolean isFav = cbFavorite.isChecked();
        int priority = rbHigh.isChecked() ? 2 : (rbMed.isChecked() ? 1 : 0);

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề công việc", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingTaskToEdit != null) {
            existingTaskToEdit.setTitle(title);
            existingTaskToEdit.setDescription(desc);
            existingTaskToEdit.setLocation(loc);
            existingTaskToEdit.setSubTasks(sub);
            existingTaskToEdit.setDateTime(selectedDateTimeStr);
            existingTaskToEdit.setPriority(priority);
            existingTaskToEdit.setCategory(selectedCategory);
            existingTaskToEdit.setRepeatType(selectedRepeat);
            existingTaskToEdit.setFavorite(isFav);

            taskViewModel.update(existingTaskToEdit);
            scheduleNotification(existingTaskToEdit);
            Toast.makeText(this, "Đã cập nhật công việc thành công!", Toast.LENGTH_SHORT).show();
        } else {
            Task newTask = new Task(title, desc, false, priority, selectedDateTimeStr, selectedCategory, isFav, loc, selectedRepeat, sub);
            taskViewModel.insert(newTask);
            scheduleNotification(newTask);
            Toast.makeText(this, "Đã thêm công việc thành công!", Toast.LENGTH_SHORT).show();
        }

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