package com.x1.groo.diary.query.repository;

import com.x1.groo.diary.query.dto.ResponseDiaryDetailDTO;
import com.x1.groo.diary.query.dto.ResponseDraftedItemsDTO;
import com.x1.groo.diary.query.dto.ResponsePersonalDiaryListDTO;
import com.x1.groo.diary.query.dto.ResponseSharedDiaryListDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QueryDiaryMapper {
    List<ResponsePersonalDiaryListDTO> getDiariesByYearMonth(
            @Param("userId") int userId,
            @Param("forestId") int forestId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    ResponseDiaryDetailDTO getDiaryDetail(
            @Param("userId") int userId,
            @Param("diaryId") int diaryId
    );

    List<ResponseSharedDiaryListDTO> getSharedDiariesByYearMonth(
            @Param("userId") int userId,
            @Param("forestId") int forestId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    List<ResponseDiaryDetailDTO> getSharedDiariesDetail(
            @Param("userId") int userId,
            @Param("diaryIds") List<Integer> diaryIds
    );

    ResponseDraftedItemsDTO getDraftedItems(
            @Param("userId") int userId,
            @Param("diaryId") int diaryId);
}
