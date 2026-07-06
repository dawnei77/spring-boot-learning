package com.dawnei.firstwebapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/tasks")
    public String tasks(Model model) {
        List<Task> taskList = taskRepository.findAll();
        model.addAttribute("tasks", taskList);
        return "tasks";
    }

    @PostMapping("/tasks/add")
    public String addTask(@RequestParam String title) {
        Task newTask = new Task(title, false);
        taskRepository.save(newTask);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/complete")
    public String completeTask(@RequestParam Long id) {
        taskRepository.findById(id).ifPresent(task -> {
            task.setDone(!task.isDone());
            taskRepository.save(task);
        });
        return "redirect:/tasks";
    }
}