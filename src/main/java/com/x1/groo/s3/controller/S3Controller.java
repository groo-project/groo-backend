package com.x1.groo.s3.controller;

import com.x1.groo.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "S3", description = "이미지 업로드 및 상태 점검기능을 제공합니다.")
@RestController
@RequestMapping("/api/image")
public class S3Controller {

    private final S3Service s3Service;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public S3Controller(S3Service s3Service, JdbcTemplate jdbcTemplate) {
        this.s3Service = s3Service;
        this.jdbcTemplate = jdbcTemplate;
    }

    // 배포 상태에서는 사용하지 않는 기능입니다.
//    @Operation(summary = "S3서버 상태 확인")
//    @GetMapping("/health2")
//    public ResponseEntity<String> health() {
//        return ResponseEntity.ok("I'm OK");
//    }
//
//    @Operation(summary = "presigned URL 발급")
//    @GetMapping("/presigned-url")
//    public ResponseEntity<String> presignedUrl(@RequestParam String fileName) {
//        String presignedUrl = s3Service.generatePresignedUrl(fileName);
//        return ResponseEntity.ok(presignedUrl);
//    }
//
//    @Operation(summary = "DB 연동 확인")
//    @GetMapping("/db-check")
//    public ResponseEntity<String> dbCheck() {
//        String result = jdbcTemplate.queryForObject("SELECT category FROM category LIMIT 1", String.class);
//        return ResponseEntity.ok("DB 연결 성공! 가져온 데이터: " + result);
//    }
//
//    @Operation(summary = "저장 이미지 목록 조회")
//    @GetMapping("/objects")
//    public ResponseEntity<?> getAllObjects(@RequestParam(required = false) String prefix) {
//        List<String> keys = s3Service.getAllObjects(prefix != null ? prefix : "");
//
//        return ResponseEntity.ok(keys);
//    }
}
