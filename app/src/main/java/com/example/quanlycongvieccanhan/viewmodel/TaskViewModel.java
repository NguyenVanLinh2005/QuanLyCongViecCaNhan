package com.example.quanlycongvieccanhan.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.quanlycongvieccanhan.data.Task;
import com.example.quanlycongvieccanhan.repository.TaskRepository;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private final LiveData<List<Task>> allTasks;

    public TaskViewModel(Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() { return allTasks; }
    public void insert(Task task) { repository.insert(task); }
    public void update(Task task) { repository.update(task); }
    public void delete(Task task) { repository.delete(task); }

    // HÀM MỚI: Giao tiếp với UI
    public void deleteCompletedTasks() { repository.deleteCompletedTasks(); }
}