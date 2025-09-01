package com.sharkskin.store.action;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello { // 將 'hi' 改為 'HelloController'

    @GetMapping("/hi")
    public String hello() {
        return "Hello from Spring Boot!";
    }
}