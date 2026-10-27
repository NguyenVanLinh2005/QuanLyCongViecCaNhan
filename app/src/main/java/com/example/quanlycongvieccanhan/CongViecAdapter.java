package com.example.quanlycongvieccanhan;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlycongvieccanhan.data.Task;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CongViecAdapter extends RecyclerView.Adapter<CongViecAdapter.TaskViewHolder> {
    private List<Task> taskList = new ArrayList<>();
    private List<Task> taskListFull = new ArrayList<>();
    private String currentQuery = "";
    private String currentFilterType = "ALL";
    private String currentDatePattern = "ALL";
    private String currentSortType = "DEFAULT";
    private final OnTaskItemClickListener listener;

    public interface OnTaskItemClickListener {
        void onDeleteClick(Task task);
        void onStatusChange(Task task, boolean isCompleted);
        void onEditClick(Task task);
        void onLongClick(Task task);
        void onFavoriteClick(Task task, boolean isFavorite);
    }

    public CongViecAdapter(OnTaskItemClickListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> tasks) {
        this.taskListFull = new ArrayList<>(tasks);
        applyFilter();
    }

    public Task getTaskAt(int position) { return taskList.get(position); }

    public void filterQuery(String text) {
        this.currentQuery = (text == null) ? "" : text.toLowerCase().trim();
        applyFilter();
    }

    public void filterType(String type) {
        this.currentFilterType = (type == null) ? "ALL" : type;
        applyFilter();
    }

    public void filterDatePattern(String datePattern) {
        this.currentDatePattern = (datePattern == null) ? "ALL" : datePattern;
        applyFilter();
    }

    public void sortType(String sort) {
        this.currentSortType = (sort == null) ? "DEFAULT" : sort;
        applyFilter();
    }

    private void applyFilter() {
        List<Task> filteredList = new ArrayList<>();
        for (Task item : taskListFull) {
            boolean matchesSearch = currentQuery.isEmpty() ||
                    item.getTitle().toLowerCase().contains(currentQuery) ||
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(currentQuery)) ||
                    (item.getLocation() != null && item.getLocation().toLowerCase().contains(currentQuery));

            if (!matchesSearch) continue;

            // Lọc theo Ngày cụ thể nếu chọn Tab Ngày
            if (!"ALL".equals(currentDatePattern)) {
                if (item.getDateTime() == null || !item.getDateTime().contains(currentDatePattern)) {
                    continue;
                }
            }

            boolean matchesType = false;
            switch (currentFilterType) {
                case "ALL": matchesType = true; break;
                case "PENDING": matchesType = !item.isCompleted(); break;
                case "COMPLETED": matchesType = item.isCompleted(); break;
                case "FAVORITE": matchesType = item.isFavorite(); break;
                case "HIGH_PRIORITY": matchesType = (item.getPriority() == 2); break;
                case "WORK": matchesType = "Công việc".equalsIgnoreCase(item.getCategory()); break;
                case "STUDY": matchesType = "Học tập".equalsIgnoreCase(item.getCategory()); break;
                case "PERSONAL": matchesType = "Cá nhân".equalsIgnoreCase(item.getCategory()); break;
                case "HEALTH": matchesType = "Sức khỏe".equalsIgnoreCase(item.getCategory()); break;
                case "FINANCE": matchesType = "Tài chính".equalsIgnoreCase(item.getCategory()); break;
                default: matchesType = true; break;
            }

            if (matchesType) {
                filteredList.add(item);
            }
        }

        if ("PRIORITY_DESC".equals(currentSortType)) {
            Collections.sort(filteredList, (t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));
        } else if ("NAME_ASC".equals(currentSortType)) {
            Collections.sort(filteredList, (t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
        } else if ("DATE_ASC".equals(currentSortType)) {
            Collections.sort(filteredList, (t1, t2) -> {
                if (t1.getDateTime() == null || t1.getDateTime().isEmpty()) return 1;
                if (t2.getDateTime() == null || t2.getDateTime().isEmpty()) return -1;
                return t1.getDateTime().compareTo(t2.getDateTime());
            });
        }

        this.taskList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = taskList.get(position);
        holder.tvTitle.setText(currentTask.getTitle());
        holder.tvDescription.setText(currentTask.getDescription());

        // Hiển thị Danh mục
        String cat = currentTask.getCategory();
        if (cat == null || cat.isEmpty()) cat = "Cá nhân";
        String catEmoji = "👤 ";
        if (cat.equalsIgnoreCase("Công việc")) catEmoji = "💼 ";
        else if (cat.equalsIgnoreCase("Học tập")) catEmoji = "📚 ";
        else if (cat.equalsIgnoreCase("Sức khỏe")) catEmoji = "🏥 ";
        else if (cat.equalsIgnoreCase("Tài chính")) catEmoji = "💰 ";
        holder.tvCategoryTag.setText(catEmoji + cat);

        // Gạch ngang và làm mờ khi đã hoàn thành
        if (currentTask.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.cardTask.setAlpha(0.6f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.cardTask.setAlpha(1.0f);
        }

        // Hiện Địa điểm nếu có
        if (currentTask.getLocation() != null && !currentTask.getLocation().isEmpty()) {
            holder.tvLocationTag.setVisibility(View.VISIBLE);
            holder.tvLocationTag.setText("📍 " + currentTask.getLocation());
        } else {
            holder.tvLocationTag.setVisibility(View.GONE);
        }

        // Hiện Chế độ Lặp nếu có
        if (currentTask.getRepeatType() != null && !currentTask.getRepeatType().isEmpty() && !"Không lặp".equalsIgnoreCase(currentTask.getRepeatType())) {
            holder.tvRepeatTag.setVisibility(View.VISIBLE);
            holder.tvRepeatTag.setText("🔄 " + currentTask.getRepeatType());
        } else {
            holder.tvRepeatTag.setVisibility(View.GONE);
        }

        // Hiện Việc con nếu có
        if (currentTask.getSubTasks() != null && !currentTask.getSubTasks().isEmpty()) {
            holder.tvSubTasksTag.setVisibility(View.VISIBLE);
            holder.tvSubTasksTag.setText("☑️ Việc con: " + currentTask.getSubTasks());
        } else {
            holder.tvSubTasksTag.setVisibility(View.GONE);
        }

        // Hiện Ngày giờ nếu có
        if (currentTask.getDateTime() != null && !currentTask.getDateTime().isEmpty()) {
            holder.tvDateTime.setVisibility(View.VISIBLE);
            holder.tvDateTime.setText("🕒 " + currentTask.getDateTime());
        } else {
            holder.tvDateTime.setVisibility(View.GONE);
        }

        // Đổi màu viền & Nhãn Mức độ ưu tiên
        if (currentTask.getPriority() == 2) {
            holder.cardTask.setStrokeColor(Color.parseColor("#EE5D50")); // Đỏ (Cao)
            holder.tvPriorityTag.setText("🔴 Ưu tiên: Cao");
            holder.tvPriorityTag.setTextColor(Color.parseColor("#EE5D50"));
        } else if (currentTask.getPriority() == 1) {
            holder.cardTask.setStrokeColor(Color.parseColor("#FFCE20")); // Vàng (Vừa)
            holder.tvPriorityTag.setText("🟡 Ưu tiên: Vừa");
            holder.tvPriorityTag.setTextColor(Color.parseColor("#D99B00"));
        } else {
            holder.cardTask.setStrokeColor(Color.parseColor("#05CD99")); // Xanh (Thấp)
            holder.tvPriorityTag.setText("🟢 Ưu tiên: Thấp");
            holder.tvPriorityTag.setTextColor(Color.parseColor("#05CD99"));
        }

        // Đổi icon Yêu thích ⭐
        if (currentTask.isFavorite()) {
            holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
        }

        holder.cbComplete.setOnCheckedChangeListener(null);
        holder.cbComplete.setChecked(currentTask.isCompleted());

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(currentTask));
        holder.btnFavorite.setOnClickListener(v -> {
            boolean newFav = !currentTask.isFavorite();
            listener.onFavoriteClick(currentTask, newFav);
        });
        holder.cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onStatusChange(currentTask, isChecked));
        holder.itemView.setOnClickListener(v -> listener.onEditClick(currentTask));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(currentTask);
            return true;
        });
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardTask;
        private final TextView tvTitle;
        private final TextView tvCategoryTag;
        private final TextView tvPriorityTag;
        private final TextView tvDescription;
        private final TextView tvLocationTag;
        private final TextView tvRepeatTag;
        private final TextView tvSubTasksTag;
        private final TextView tvDateTime;
        private final CheckBox cbComplete;
        private final ImageButton btnFavorite;
        private final ImageButton btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask = itemView.findViewById(R.id.cardTask);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategoryTag = itemView.findViewById(R.id.tvCategoryTag);
            tvPriorityTag = itemView.findViewById(R.id.tvPriorityTag);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocationTag = itemView.findViewById(R.id.tvLocationTag);
            tvRepeatTag = itemView.findViewById(R.id.tvRepeatTag);
            tvSubTasksTag = itemView.findViewById(R.id.tvSubTasksTag);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            cbComplete = itemView.findViewById(R.id.cbComplete);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}