package com.x1.groo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "health check", description = "just for health check")
@RestController
@RequestMapping("/api/health")
public class GrooController {
    @Operation(summary = "health check")
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok(LocalDateTime.now() + " I'm OK" );
    }
}
