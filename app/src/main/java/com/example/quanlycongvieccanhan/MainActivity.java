package com.example.quanlycongvieccanhan;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import androidx.appcompat.app.AppCompatDelegate;
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
import com.example.quanlycongvieccanhan.data.SQLiteViewerActivity;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.viewmodel.TaskViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private CongViecAdapter adapter;

    private TextView tvHeaderDate;
    private TextView tvHeaderStatus;
    private ProgressBar headerProgressBar;
    private DrawerLayout drawerLayout;

    private final List<String> dateTabPatterns = new ArrayList<>();

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

        // Lắng nghe sự kiện chọn mục trên Navigation Drawer -> Mở Màn hình tương ứng
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);
            if (id == R.id.nav_ai) {
                startActivity(new Intent(this, AiPlannerActivity.class));
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, ThemCongViecActivity.class));
                return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, DanhMucActivity.class));
                return true;
            } else if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, LichActivity.class));
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, ThongKeActivity.class));
                return true;
            } else if (id == R.id.nav_clear) {
                startActivity(new Intent(this, DonDepActivity.class));
                return true;
            } else if (id == R.id.nav_backup) {
                startActivity(new Intent(this, SaoLuuActivity.class));
                return true;
            } else if (id == R.id.nav_theme) {
                toggleDarkMode();
                return true;
            } else if (id == R.id.nav_info) {
                startActivity(new Intent(this, ThongTinActivity.class));
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

        adapter = new CongViecAdapter(new CongViecAdapter.OnTaskItemClickListener() {
            @Override
            public void onDeleteClick(Task task) { deleteTaskWithUndo(task); }

            @Override
            public void onStatusChange(Task task, boolean isCompleted) {
                task.setCompleted(isCompleted);
                taskViewModel.update(task);
            }

            @Override
            public void onEditClick(Task task) {
                Intent intent = new Intent(MainActivity.this, ChiTietActivity.class);
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

        // Thanh Lọc 7 Ngày Trong Tuần (Weekly Calendar Tabs)
        TabLayout tabLayoutFilter = findViewById(R.id.tabLayoutFilter);
        TextView tvFilterSummary = findViewById(R.id.tvFilterSummary);
        MaterialButton btnFilterCategory = findViewById(R.id.btnFilterCategory);

        setupWeeklyDateTabs(tabLayoutFilter);

        tabLayoutFilter.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    adapter.filterDatePattern("ALL");
                } else if (position < dateTabPatterns.size()) {
                    String datePattern = dateTabPatterns.get(position);
                    adapter.filterDatePattern(datePattern);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnFilterCategory.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("📌 Tất cả danh mục");
            popup.getMenu().add("⏳ Công việc đang chờ");
            popup.getMenu().add("✅ Công việc đã xong");
            popup.getMenu().add("⭐ Công việc yêu thích");
            popup.getMenu().add("🔴 Ưu tiên Cao");
            popup.getMenu().add("💼 Công việc");
            popup.getMenu().add("📚 Học tập");
            popup.getMenu().add("👤 Cá nhân");
            popup.getMenu().add("🏥 Sức khỏe");
            popup.getMenu().add("💰 Tài chính");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle() != null ? item.getTitle().toString() : "";
                if ("⏳ Công việc đang chờ".equals(title)) {
                    adapter.filterType("PENDING");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Lọc: Đang chờ");
                } else if ("✅ Công việc đã xong".equals(title)) {
                    adapter.filterType("COMPLETED");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Lọc: Đã xong");
                } else if ("⭐ Công việc yêu thích".equals(title)) {
                    adapter.filterType("FAVORITE");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Lọc: Yêu thích");
                } else if ("🔴 Ưu tiên Cao".equals(title)) {
                    adapter.filterType("HIGH_PRIORITY");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Lọc: Ưu tiên Cao");
                } else if ("💼 Công việc".equals(title)) {
                    adapter.filterType("WORK");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Lọc: Công việc");
                } else if ("📚 Học tập".equals(title)) {
                    adapter.filterType("STUDY");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Lọc: Học tập");
                } else if ("👤 Cá nhân".equals(title)) {
                    adapter.filterType("PERSONAL");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Lọc: Cá nhân");
                } else if ("🏥 Sức khỏe".equals(title)) {
                    adapter.filterType("HEALTH");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Lọc: Sức khỏe");
                } else if ("💰 Tài chính".equals(title)) {
                    adapter.filterType("FINANCE");
                    if (tvFilterSummary != null) tvFilterSummary.setText("📌 Lọc: Tài chính");
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
        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, ThemCongViecActivity.class)));

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

    private void setupWeeklyDateTabs(TabLayout tabLayout) {
        tabLayout.removeAllTabs();
        dateTabPatterns.clear();

        // Tab 0: Tất cả
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        dateTabPatterns.add("ALL");

        SimpleDateFormat tabTitleFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat datePatternFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("E", new Locale("vi", "VN"));

        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            Date date = cal.getTime();
            String dateFormatted = tabTitleFormat.format(date);
            String fullDatePattern = datePatternFormat.format(date);

            String label;
            if (i == 0) {
                label = dateFormatted + " (Hôm nay)";
            } else {
                label = dateFormatted + " (" + dayOfWeekFormat.format(date) + ")";
            }

            tabLayout.addTab(tabLayout.newTab().setText(label));
            dateTabPatterns.add(fullDatePattern);

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void toggleDarkMode() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "Đã chuyển sang Giao diện Sáng ☀️", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(this, "Đã chuyển sang Giao diện Tối 🌙", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSortDialog() {
        String[] options = {"📌 Mặc định (Yêu thích & Ưu tiên)", "🔴 Theo Mức ưu tiên (Cao -> Thấp)", "🕒 Theo Hạn chót (Gần nhất)", "🔤 Theo Tên công việc (A -> Z)"};
        new AlertDialog.Builder(this)
                .setTitle("📶 Sắp Xếp Danh Sách Công Việc")
                .setItems(options, (dialog, which) -> {
                    if (which == 1) {
                        adapter.sortType("PRIORITY_DESC");
                        Toast.makeText(this, "Đã sắp xếp theo Mức ưu tiên Cao", Toast.LENGTH_SHORT).show();
                    } else if (which == 2) {
                        adapter.sortType("DATE_ASC");
                        Toast.makeText(this, "Đã sắp xếp theo Hạn chót gần nhất", Toast.LENGTH_SHORT).show();
                    } else if (which == 3) {
                        adapter.sortType("NAME_ASC");
                        Toast.makeText(this, "Đã sắp xếp theo Tên A-Z", Toast.LENGTH_SHORT).show();
                    } else {
                        adapter.sortType("DEFAULT");
                        Toast.makeText(this, "Đã khôi phục sắp xếp mặc định", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
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
            startActivity(new Intent(this, LichActivity.class));
            return true;
        } else if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (id == R.id.action_stats) {
            startActivity(new Intent(this, ThongKeActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}