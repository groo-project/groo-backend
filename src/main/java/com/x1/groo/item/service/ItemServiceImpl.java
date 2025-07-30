package com.x1.groo.item.service;

import com.x1.groo.item.dto.CategoryDTO;
import com.x1.groo.item.dto.CategoryEmotionItemDTO;
import com.x1.groo.item.repository.ItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemMapper itemMapper;

    @Autowired
    public ItemServiceImpl(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    @Override
    public List<CategoryDTO> findAllCategories() {
        List<CategoryDTO> categoryDTOList = itemMapper.selectAllCategories();
        return categoryDTOList;
    }

    @Override
    public List<CategoryEmotionItemDTO> findItemsByCategoryAndEmotion(int categoryId, String mainEmotion) {
        List<CategoryEmotionItemDTO> itemDTOList = itemMapper.selectItemsByCategoryAndEmotion(categoryId, mainEmotion);

        return itemDTOList;
    }
}
