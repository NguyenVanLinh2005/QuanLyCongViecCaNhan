package com.example.quanlycongvieccanhan.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    // Sắp xếp: Yêu thích (⭐) lên trước -> Việc chưa xong (0) -> Ưu tiên cao (2) -> Mới nhất
    @Query("SELECT * FROM task_table ORDER BY isFavorite DESC, isCompleted ASC, priority DESC, id DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("DELETE FROM task_table WHERE isCompleted = 1")
    void deleteCompletedTasks();
}