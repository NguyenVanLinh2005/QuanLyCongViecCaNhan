package com.example.quanlycongvieccanhan.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_table")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private boolean isCompleted;
    private int priority;      // 0: Thấp, 1: Vừa, 2: Cao
    private String dateTime;   // Lưu chuỗi ngày giờ
    private String category;   // "Cá nhân", "Công việc", "Học tập", "Sức khỏe", "Tài chính"
    private boolean isFavorite;// Trạng thái yêu thích ⭐
    private String location;   // Địa điểm thực hiện (Ví dụ: "Phòng A201")
    private String repeatType; // "Không lặp", "Hằng ngày", "Hằng tuần", "Hằng tháng"
    private String subTasks;   // Danh sách việc con (Chuỗi phân cách bằng dấu phẩy)

    public Task(String title, String description, boolean isCompleted, int priority, String dateTime, String category, boolean isFavorite, String location, String repeatType, String subTasks) {
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.priority = priority;
        this.dateTime = dateTime;
        this.category = (category != null && !category.isEmpty()) ? category : "Cá nhân";
        this.isFavorite = isFavorite;
        this.location = (location != null) ? location : "";
        this.repeatType = (repeatType != null && !repeatType.isEmpty()) ? repeatType : "Không lặp";
        this.subTasks = (subTasks != null) ? subTasks : "";
    }

    @Ignore
    public Task(String title, String description, boolean isCompleted, int priority, String dateTime, String category, boolean isFavorite) {
        this(title, description, isCompleted, priority, dateTime, category, isFavorite, "", "Không lặp", "");
    }

    @Ignore
    public Task(String title, String description, boolean isCompleted, int priority, String dateTime) {
        this(title, description, isCompleted, priority, dateTime, "Cá nhân", false, "", "Không lặp", "");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getRepeatType() { return repeatType; }
    public void setRepeatType(String repeatType) { this.repeatType = repeatType; }

    public String getSubTasks() { return subTasks; }
    public void setSubTasks(String subTasks) { this.subTasks = subTasks; }
}