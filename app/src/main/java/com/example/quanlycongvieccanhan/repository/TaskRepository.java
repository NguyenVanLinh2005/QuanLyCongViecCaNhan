package com.example.quanlycongvieccanhan.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.data.TaskDao;
import com.example.quanlycongvieccanhan.data.TaskDatabase;
import java.util.List;

public class TaskRepository {
    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;

    public TaskRepository(Application application) {
        TaskDatabase db = TaskDatabase.getDatabase(application);
        taskDao = db.taskDao();
        allTasks = taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() { return allTasks; }
    public void insert(Task task) { TaskDatabase.databaseWriteExecutor.execute(() -> taskDao.insertTask(task)); }
    public void update(Task task) { TaskDatabase.databaseWriteExecutor.execute(() -> taskDao.updateTask(task)); }
    public void delete(Task task) { TaskDatabase.databaseWriteExecutor.execute(() -> taskDao.deleteTask(task)); }

    // HÀM MỚI: Đẩy lệnh xóa vào luồng chạy ngầm
    public void deleteCompletedTasks() { TaskDatabase.databaseWriteExecutor.execute(() -> taskDao.deleteCompletedTasks()); }
}