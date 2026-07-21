package com.dawnei.firstwebapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    // Full page load — same as before, unchanged.
    @GetMapping("/tasks")
    public String tasks(Model model) {
        User loggedInUser = getLoggedInUser();
        List<Task> taskList = taskRepository.findByUser(loggedInUser);
        model.addAttribute("tasks", taskList);
        model.addAttribute("username", loggedInUser.getUsername());
        return "tasks";
    }

    // ---- AJAX endpoints below: called by fetch() from tasks.js ----
    // @ResponseBody + a DTO return type means Spring serializes just the
    // fields the frontend needs, as JSON — no redirect, no full reload.

    @PostMapping("/tasks/add")
    @ResponseBody
    public TaskDto addTask(@RequestParam String title) {
        User loggedInUser = getLoggedInUser();
        Task newTask = new Task(title.trim(), false);
        newTask.setUser(loggedInUser);
        taskRepository.save(newTask);
        return new TaskDto(newTask);
    }

    @PostMapping("/tasks/complete")
    @ResponseBody
    public TaskDto completeTask(@RequestParam Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        assertOwnership(task);
        task.setDone(!task.isDone());
        taskRepository.save(task);
        return new TaskDto(task);
    }

    @PostMapping("/tasks/delete")
    @ResponseBody
    public void deleteTask(@RequestParam Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        assertOwnership(task);
        taskRepository.deleteById(id);
    }

    // NEW: wasn't there before — stops one logged-in user from
    // completing/deleting another user's task by guessing an id.
    private void assertOwnership(Task task) {
        User loggedInUser = getLoggedInUser();
        if (!task.getUser().getId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException("This task does not belong to you");
        }
    }

    // Unchanged from your version — this one SHOULD stay a normal
    // redirecting POST, since deleting the account navigates the user
    // away to /login anyway; no AJAX needed here.
    @PostMapping("/account/delete")
    public String deleteAccount() {
        User loggedInUser = getLoggedInUser();
        userRepository.delete(loggedInUser);
        return "redirect:/login?deleted";
    }
}
