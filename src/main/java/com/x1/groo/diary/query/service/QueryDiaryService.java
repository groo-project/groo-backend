package com.x1.groo.diary.query.service;

import com.x1.groo.diary.query.dto.ResponseDiaryDetailDTO;
import com.x1.groo.diary.query.dto.ResponseDraftedItemsDTO;
import com.x1.groo.diary.query.dto.ResponsePersonalDiaryListDTO;
import com.x1.groo.diary.query.dto.ResponseSharedDiaryListDTO;

import java.util.List;

public interface QueryDiaryService {
    List<ResponsePersonalDiaryListDTO> getDiariesByYearMonth(int userId, int forestId, int year, int month);

    ResponseDiaryDetailDTO getDiaryDetail(int userId, int diaryId);

    List<ResponseSharedDiaryListDTO> getSharedDiariesByYearMonth(int userId, int forestId, int year, int month);

    List<ResponseDiaryDetailDTO> getSharedDiariesDetail(int userId, List<Integer> diaryIds);

    ResponseDraftedItemsDTO getDraftedItems(int userId, int diaryId);
}
