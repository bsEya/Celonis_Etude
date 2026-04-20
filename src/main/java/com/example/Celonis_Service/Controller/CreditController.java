package com.example.Celonis_Service.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/credit-check")
public class CreditController {

    @GetMapping
    public Map<String, Object> check(
            @RequestParam int salary,
            @RequestParam int amount
    ) {

        int score = salary > 1200 ? 750 : 600;
        boolean fraud = false;

        return Map.of(
                "creditScore", score,
                "fraudFlag", fraud
        );
    }
}