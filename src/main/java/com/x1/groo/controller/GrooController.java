package com.x1.groo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GrooController {

    @GetMapping("/health")
    public String health() {
        return "I'm OK6";
    }
}
