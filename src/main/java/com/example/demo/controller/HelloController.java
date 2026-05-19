package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // SWE304 Project 4 - single end-point, no DB.
    @GetMapping("/")
    public String hello() {
        String pod = System.getenv("HOSTNAME");
        if (pod == null || pod.isBlank()) {
            pod = "local";
        }
        return "Hello from SWE304 DEVOPS4! Spring Boot running on Kubernetes. Served by pod: " + pod;
    }

}
