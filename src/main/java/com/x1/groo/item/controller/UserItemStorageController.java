package com.x1.groo.item.controller;

import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.item.service.UserItemStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 아이템 저장", description = "사용자가 소유하고 있는 아이템 정보에 대한 기능을 제공합니다.")
@RestController
@RequestMapping("/api")
@Slf4j
public class UserItemStorageController {

    private final UserItemStorageService userItemStorageService;

    @Autowired
    public UserItemStorageController(UserItemStorageService userItemStorageService) {
        this.userItemStorageService = userItemStorageService;
    }

    @Operation(summary = "소유 아이템 저장", description = "아이템 획득 시 나의 보관소에 아이템이 추가됩니다.")
    @PostMapping("/item-storage")
    public ResponseEntity<Void> saveItemToStorage(@AuthenticationPrincipal CustomUserDetails user,
                                                  @RequestParam int itemId, @RequestParam int forestId) {

        int userId = user.getUserId();

        userItemStorageService.saveItemToStorage(userId, itemId, forestId);

        return ResponseEntity.ok().build();
    }
}
