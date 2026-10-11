package com.example.quanlycongvieccanhan;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.viewmodel.TaskViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private TaskAdapter adapter;

    private TextView tvHeaderDate;
    private TextView tvHeaderStatus;
    private ProgressBar headerProgressBar;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo kênh thông báo và xin quyền
        createNotificationChannel();
        checkNotificationPermission();

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Đặt Nút Menu 3 gạch bên TRÁI thanh Toolbar & Mở Navigation Drawer khi click
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Lắng nghe sự kiện chọn mục trên Navigation Drawer -> Mở Màn hình Chuyên biệt tương ứng
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);
            if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddTaskActivity.class));
                return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, CategoryActivity.class));
                return true;
            } else if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, ThongKeActivity.class));
                return true;
            } else if (id == R.id.nav_clear) {
                startActivity(new Intent(this, CleanCompletedActivity.class));
                return true;
            } else if (id == R.id.nav_backup) {
                startActivity(new Intent(this, BackupRestoreActivity.class));
                return true;
            } else if (id == R.id.nav_info) {
                startActivity(new Intent(this, AppInfoActivity.class));
                return true;
            }
            return false;
        });

        // Xử lý nút Back trên Android
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Binding Header Views
        tvHeaderDate = findViewById(R.id.tvHeaderDate);
        tvHeaderStatus = findViewById(R.id.tvHeaderStatus);
        headerProgressBar = findViewById(R.id.headerProgressBar);

        SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        if (tvHeaderDate != null) {
            tvHeaderDate.setText("📅 " + sdfDate.format(new Date()));
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(new TaskAdapter.OnTaskItemClickListener() {
            @Override
            public void onDeleteClick(Task task) { deleteTaskWithUndo(task); }

            @Override
            public void onStatusChange(Task task, boolean isCompleted) {
                task.setCompleted(isCompleted);
                taskViewModel.update(task);
            }

            @Override
            public void onEditClick(Task task) {
                Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
                intent.putExtra("taskId", task.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Task task, boolean isFavorite) {
                task.setFavorite(isFavorite);
                taskViewModel.update(task);
            }

            @Override
            public void onLongClick(Task task) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String shareText = "Công việc: " + task.getTitle() + "\nChi tiết: " + task.getDescription();
                if (task.getDateTime() != null && !task.getDateTime().isEmpty()) {
                    shareText += "\nHạn chót: " + task.getDateTime();
                }
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Chia sẻ công việc qua..."));
            }
        });
        recyclerView.setAdapter(adapter);
        View layoutEmpty = findViewById(R.id.layoutEmpty);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(this, tasks -> {
            adapter.setTasks(tasks);
            if (tasks == null || tasks.isEmpty()) {
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);

                if (tvHeaderStatus != null) tvHeaderStatus.setText("0/0 Hoàn thành (0%)");
                if (headerProgressBar != null) headerProgressBar.setProgress(0);
            } else {
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                int total = tasks.size();
                int completed = 0;
                for (Task t : tasks) {
                    if (t.isCompleted()) completed++;
                }
                int percent = (total > 0) ? (completed * 100 / total) : 0;
                if (tvHeaderStatus != null) {
                    tvHeaderStatus.setText(completed + "/" + total + " Hoàn thành (" + percent + "%)");
                }
                if (headerProgressBar != null) {
                    headerProgressBar.setProgress(percent);
                }
            }
        });

        // Thanh Lọc Gọn Gàng (TabLayout & Category Dropdown Button)
        TabLayout tabLayoutFilter = findViewById(R.id.tabLayoutFilter);
        TextView tvFilterSummary = findViewById(R.id.tvFilterSummary);
        MaterialButton btnFilterCategory = findViewById(R.id.btnFilterCategory);

        tabLayoutFilter.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 1) adapter.filterType("PENDING");
                else if (position == 2) adapter.filterType("COMPLETED");
                else if (position == 3) adapter.filterType("FAVORITE");
                else adapter.filterType("ALL");
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnFilterCategory.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("📌 Tất cả danh mục");
            popup.getMenu().add("🔴 Ưu tiên Cao");
            popup.getMenu().add("💼 Công việc");
            popup.getMenu().add("📚 Học tập");
            popup.getMenu().add("👤 Cá nhân");
            popup.getMenu().add("🏥 Sức khỏe");
            popup.getMenu().add("💰 Tài chính");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle() != null ? item.getTitle().toString() : "";
                if ("🔴 Ưu tiên Cao".equals(title)) {
                    adapter.filterType("HIGH_PRIORITY");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Ưu tiên Cao");
                } else if ("💼 Công việc".equals(title)) {
                    adapter.filterType("WORK");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Công việc");
                } else if ("📚 Học tập".equals(title)) {
                    adapter.filterType("STUDY");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Học tập");
                } else if ("👤 Cá nhân".equals(title)) {
                    adapter.filterType("PERSONAL");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Cá nhân");
                } else if ("🏥 Sức khỏe".equals(title)) {
                    adapter.filterType("HEALTH");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Sức khỏe");
                } else if ("💰 Tài chính".equals(title)) {
                    adapter.filterType("FINANCE");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Tài chính");
                } else {
                    adapter.filterType("ALL");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Tất cả danh mục");
                }
                return true;
            });
            popup.show();
        });

        // Mở Màn hình Thêm công việc riêng biệt khi bấm FAB nút +
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddTaskActivity.class)));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Task deletedTask = adapter.getTaskAt(viewHolder.getAdapterPosition());
                deleteTaskWithUndo(deletedTask);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "task_channel", "Thông báo Hạn chót",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Cảnh báo trước 5 phút khi công việc đến hạn");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void deleteTaskWithUndo(Task task) {
        taskViewModel.delete(task);
        Snackbar.make(findViewById(R.id.mainContent), "Đã xóa: " + task.getTitle(), Snackbar.LENGTH_LONG)
                .setAction("HOÀN TÁC", v -> taskViewModel.insert(task))
                .setActionTextColor(Color.parseColor("#FFCE20"))
                .show();
    }

    private void exportAndShareTasks() {
        List<Task> tasks = taskViewModel.getAllTasks().getValue();
        if (tasks == null || tasks.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu công việc để sao lưu", Toast.LENGTH_SHORT).show();
            return;
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

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Sao lưu & Chia sẻ danh sách công việc"));
    }

    private void showAppInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("ℹ️ THÔNG TIN ĐỒ ÁN")
                .setMessage("📱 Ứng Dụng Quản Lý Công Việc Cá Nhân\n\n" +
                        "📌 Đề tài: Xây dựng ứng dụng di động quản lý công việc cá nhân\n" +
                        "⚙️ Cấu trúc: MVVM Architecture + Room Database\n" +
                        "🔔 Tính năng nổi bật: Màn hình Thêm/Sửa riêng, Màn hình Chi tiết công việc, Màn hình Lịch, Màn hình Danh mục, Báo thức nhắc nhở, Việc con (Sub-tasks), Lặp lại công việc, Địa điểm, Thống kê Tiến độ, Sao lưu Báo cáo.\n\n" +
                        "© 2026 - Tiểu luận Phát triển Ứng dụng Di động")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void resetAllData() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Xóa toàn bộ dữ liệu")
                .setMessage("Bạn có chắc chắn muốn XÓA TOÀN BỘ công việc không? Hành động này không thể hoàn tác!")
                .setPositiveButton("XÓA TẤT CẢ", (dialog, which) -> {
                    List<Task> tasks = taskViewModel.getAllTasks().getValue();
                    if (tasks != null) {
                        for (Task t : tasks) {
                            taskViewModel.delete(t);
                        }
                    }
                    Toast.makeText(this, "Đã xóa toàn bộ dữ liệu công việc", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setQueryHint("Nhập tên công việc...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }
                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.filterQuery(newText);
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_calendar) {
            startActivity(new Intent(this, CalendarActivity.class));
            return true;
        } else if (id == R.id.action_stats) {
            startActivity(new Intent(this, ThongKeActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}