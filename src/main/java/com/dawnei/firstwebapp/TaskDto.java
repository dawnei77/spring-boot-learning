package com.dawnei.firstwebapp;

// Only what the frontend needs — never return Task directly as JSON,
// since Task -> User -> tasks -> Task ... would recurse forever via Jackson,
// and would also serialize the user's password hash.
public record TaskDto(Long id, String title, boolean done) {
    public TaskDto(Task task) {
        this(task.getId(), task.getTitle(), task.isDone());
    }
}
