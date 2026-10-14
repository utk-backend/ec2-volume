package com.example.ec2volume.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InstanceController {
    private static final String MESSAGE = "message";
    private static final String STATUS = "status";

    @GetMapping("/hello")
    public Map<String, String> helloWorld() {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, "hello-world");
        response.put(STATUS, "200 OK");
        return response;
    }
}
