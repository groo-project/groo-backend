package com.x1.groo.item.controller;

import com.x1.groo.item.dto.CategoryDTO;
import com.x1.groo.item.dto.CategoryEmotionItemDTO;
import com.x1.groo.item.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@Tag( name = "아이템 정보 조회", description = "아이템 정보를 조회하는 기능을 제공합니다.")
@RestController
@RequestMapping("/api")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(summary = "카테고리(식물, 사물, 기타) 조회")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = itemService.findAllCategories();

        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "카테고리 별 감정 아이템 조회")
    @GetMapping("/items")
    public ResponseEntity<List<CategoryEmotionItemDTO>> getItemsByCategoryAndEmotion(
            @RequestParam int categoryId,
            @RequestParam String mainEmotion) {
        List<CategoryEmotionItemDTO> items = itemService.findItemsByCategoryAndEmotion(categoryId, mainEmotion);

        return ResponseEntity.ok(items);
    }
}
