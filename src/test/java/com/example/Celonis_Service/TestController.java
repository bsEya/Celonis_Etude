package com.example.Celonis_Service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "Application OK 🚀";
    }

    @GetMapping("/test")
    public String test() {
        return "Test endpoint works 👍";
    }
}
