package com.dawnei.firstwebapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from the API!";
    }

    @GetMapping("/api/user")
    public User getUser() {
        return new User("Dawn", 21);
    }
}
