package com.jira.jira.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.jira.jira.models.Task;
import com.jira.jira.repositories.ProjectRepository;
import com.jira.jira.repositories.TaskRepository;
import com.jira.jira.service.TaskService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/project/{projectId}")
    public ModelAndView getTasksByProject(@PathVariable Long projectId) {
        ModelAndView mav = new ModelAndView("tasks");
        mav.addObject("tasks", taskService.getTasksByProject(projectId));
        mav.addObject("project", projectRepository.findById(projectId).orElse(null));
        return mav;
    }

    @GetMapping("/view/{projectId}")
    public ModelAndView viewProject(@PathVariable Long projectId) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        project.setTasks(taskService.getTasksByProject(projectId));
        ModelAndView mav = new ModelAndView("view-project");
        mav.addObject("project", project);
        mav.addObject("allStatuses", Task.Status.values());
        return mav;
    }

    @PostMapping("/add")
    public String addTask(@RequestParam Long projectId,
                          @RequestParam String status,
                          @RequestParam int storyPoints,
                          Task task) {
        try {
            taskService.addTask(task, projectId, status, storyPoints);
        } catch (IllegalArgumentException e) {
            return "redirect:/projects/view/" + projectId + "?error=invalid_status";
        }
        return "redirect:/projects/view/" + projectId;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView editTaskForm(@PathVariable Long id) {
        Task task = taskService.getTaskWithProject(id);
        ModelAndView mav = new ModelAndView("edit-task");
        if (task != null) {
            mav.addObject("task", task);
            mav.addObject("project", task.getProject());
            mav.addObject("allStatuses", Task.Status.values());
        }
        return mav;
    }

    @PostMapping("/edit/{id}")
    public String editTask(@PathVariable Long id,
                           @Valid @ModelAttribute("task") Task updatedTask,
                           BindingResult result) {
        if (result.hasErrors()) {
            return "edit-task";
        }
        try {
            taskService.updateTask(id, updatedTask, updatedTask.getStatus().toString());
        } catch (IllegalArgumentException e) {
            Task task = taskService.getTaskById(id);
            Long projectId = (task != null && task.getProject() != null) ? task.getProject().getId() : null;
            return "redirect:/projects/view/" + projectId + "?error=invalid_status";
        }
        Task task = taskService.getTaskById(id);
        return "redirect:/projects/view/" + (task != null && task.getProject() != null ? task.getProject().getId() : "");
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        Long projectId = (task != null && task.getProject() != null) ? task.getProject().getId() : null;
        taskService.deleteTask(id);
        return "redirect:/projects/view/" + (projectId != null ? projectId : "");
    }

    @PostMapping("/update-status/{taskId}")
    @ResponseBody
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long taskId,
                                              @RequestParam String newStatus) {
        try {
            taskService.updateTaskStatus(taskId, newStatus);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value");
        }
    }
}
