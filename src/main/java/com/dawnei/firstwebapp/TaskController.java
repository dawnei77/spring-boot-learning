package com.dawnei.firstwebapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private UserRepository userRepository;

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    @GetMapping("/tasks")
    public String tasks(Model model) {
        User loggedInUser = getLoggedInUser();
        List<Task> taskList = taskRepository.findByUser(loggedInUser);
        model.addAttribute("tasks", taskList);
        return "tasks";
    }

    @PostMapping("/tasks/add")
    public String addTask(@RequestParam String title) {
        User loggedInUser = getLoggedInUser();
        Task newTask = new Task(title, false);
        newTask.setUser(loggedInUser);
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

    @PostMapping("/tasks/delete")
    public String deleteTask(@RequestParam Long id) {
        taskRepository.deleteById(id);
        return "redirect:/tasks";
    }

    @PostMapping("/account/delete")
    public String deleteAccount() {
        User loggedInUser = getLoggedInUser();
        userRepository.delete(loggedInUser);
        return "redirect:/login?deleted";
    }
}