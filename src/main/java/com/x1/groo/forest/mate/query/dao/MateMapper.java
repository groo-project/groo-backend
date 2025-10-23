package com.x1.groo.forest.mate.query.dao;

import com.x1.groo.forest.emotion.query.dto.PlacementDTO;
import com.x1.groo.forest.mate.query.dto.MateForestDetailDTO;
import com.x1.groo.forest.mate.query.dto.MateForestResponseDTO;
import com.x1.groo.forest.mate.query.dto.MateItemDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MateMapper {

    List<MateForestResponseDTO> findForestsByUserId(
            @Param("userId") int userId
    );

    MateForestDetailDTO findForestBaseDetail(
            @Param("forestId") int forestId);

    // 배치 목록 조회
    List<PlacementDTO> findPlacementListByForestId(
            @Param("forestId") int forestId);

    List<String> findNicknamesByForestId(
            @Param("forestId") int forestId
    );

    List<MateItemDTO> getPieceOfMemory(
            @Param("userId") int userId,
            @Param("categoryId") int categoryId,
            @Param("forestId") int forestId
    );

    int getWrittenDiaryCountByForestId(@Param("forestId") int forestId);
}


