package com.x1.groo.s3.controller;

import com.x1.groo.s3.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/image")
public class S3Controller {

    private final S3Service s3Service;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public S3Controller(S3Service s3Service, JdbcTemplate jdbcTemplate) {
        this.s3Service = s3Service;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health2")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("I'm OK");
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<String> presignedUrl(@RequestParam String fileName) {
        String presignedUrl = s3Service.generatePresignedUrl(fileName);
        return ResponseEntity.ok(presignedUrl);
    }

    @GetMapping("/db-check")
    public ResponseEntity<String> dbCheck() {
        String result = jdbcTemplate.queryForObject("SELECT category FROM category LIMIT 1", String.class);
        return ResponseEntity.ok("DB 연결 성공! 가져온 데이터: " + result);
    }

    @GetMapping("/objects")
    public ResponseEntity<?> getAllObjects(@RequestParam(required = false) String prefix) {
        List<String> keys = s3Service.getAllObjects(prefix != null ? prefix : "");

        return ResponseEntity.ok(keys);
    }
}
