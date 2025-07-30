package com.x1.groo.item.controller;

import com.x1.groo.item.dto.CategoryDTO;
import com.x1.groo.item.dto.CategoryEmotionItemDTO;
import com.x1.groo.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // 카테고리(식물, 사물, 기타) 조회
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = itemService.findAllCategories();

        return ResponseEntity.ok(categories);
    }

    // 카테고리 별 감정 아이템 조회
    @GetMapping("/items")
    public ResponseEntity<List<CategoryEmotionItemDTO>> getItemsByCategoryAndEmotion(
            @RequestParam int categoryId,
            @RequestParam String mainEmotion) {
        List<CategoryEmotionItemDTO> items = itemService.findItemsByCategoryAndEmotion(categoryId, mainEmotion);

        return ResponseEntity.ok(items);
    }
}
