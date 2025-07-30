package com.x1.groo.item.controller;

import com.x1.groo.common.JwtUtil;
import com.x1.groo.item.service.UserItemStorageService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Slf4j
public class UserItemStorageController {

    private final UserItemStorageService userItemStorageService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserItemStorageController(UserItemStorageService userItemStorageService, JwtUtil jwtUtil) {
        this.userItemStorageService = userItemStorageService;
        this.jwtUtil = jwtUtil;
    }

    // 아이템 획득 시 나의 보관소에 아이템이 추가됨
    @PostMapping("/item-storage")
    public ResponseEntity<Void> saveItemToStorage(@RequestHeader(value = "Authorization") String authorizationHeader,
                                                  @RequestParam int itemId, @RequestParam int forestId) {

        // "Bearer " 부분 제거
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        log.info("userId = {}", userId);

        userItemStorageService.saveItemToStorage(userId, itemId, forestId);

        return ResponseEntity.ok().build();
    }
}
