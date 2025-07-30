package com.x1.groo.item.repository;

import com.x1.groo.item.dto.CategoryDTO;
import com.x1.groo.item.dto.CategoryEmotionItemDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper {

    List<CategoryDTO> selectAllCategories();

    List<CategoryEmotionItemDTO> selectItemsByCategoryAndEmotion(@Param("categoryId") int categoryId, @Param("mainEmotion") String mainEmotion);

}
