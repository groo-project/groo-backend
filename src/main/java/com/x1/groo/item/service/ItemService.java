package com.x1.groo.item.service;

import com.x1.groo.item.dto.CategoryDTO;
import com.x1.groo.item.dto.CategoryEmotionItemDTO;

import java.util.List;

public interface ItemService {
    List<CategoryDTO>  findAllCategories();

    List<CategoryEmotionItemDTO> findItemsByCategoryAndEmotion(int categoryId, String mainEmotion);
}
