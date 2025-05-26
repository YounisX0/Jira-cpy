package com.jira.jira.service;

import com.jira.jira.models.Task;
import com.jira.jira.repositories.ProjectRepository;
import com.jira.jira.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectIdOrderByStoryPointsDesc(projectId);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task getTaskWithProject(Long id) {
        return taskRepository.findByIdWithProject(id);
    }

    public Task.Status convertToTaskStatus(String status) {
        String normalized = status.trim().toUpperCase().replace(" ", "_");
        return Task.Status.valueOf(normalized); // Let controller catch this if invalid
    }

    public void addTask(Task task, Long projectId, String status, int storyPoints) {
        task.setStatus(convertToTaskStatus(status));
        task.setStoryPoints(storyPoints);
        task.setProject(projectRepository.findById(projectId).orElse(null));
        taskRepository.save(task);
    }

    public void updateTask(Long id, Task updatedTask, String status) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setStatus(convertToTaskStatus(status));
            task.setStoryPoints(updatedTask.getStoryPoints());
            taskRepository.save(task);
        }
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            taskRepository.delete(task);
        }
    }

    public void updateTaskStatus(Long id, String status) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            task.setStatus(convertToTaskStatus(status));
            taskRepository.save(task);
        }
    }
}
