package com.x1.groo.diary.query.service;

import com.x1.groo.diary.query.dto.ResponseDiaryDetailDTO;
import com.x1.groo.diary.query.dto.ResponseDraftedItemsDTO;
import com.x1.groo.diary.query.dto.ResponsePersonalDiaryListDTO;
import com.x1.groo.diary.query.dto.ResponseSharedDiaryListDTO;
import com.x1.groo.diary.query.repository.QueryDiaryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueryDiaryServiceImpl implements QueryDiaryService {

    @Autowired
    private QueryDiaryMapper queryDiaryMapper;

    @Override
    public List<ResponsePersonalDiaryListDTO> getDiariesByYearMonth(int userId, int forestId, int year, int month) {

        LocalDateTime startDateTime = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endDateTime = startDateTime
                .withDayOfMonth(startDateTime.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        return queryDiaryMapper.getDiariesByYearMonth(userId, forestId, startDateTime, endDateTime);
    }

    @Override
    public ResponseDiaryDetailDTO getDiaryDetail(int userId, int diaryId) {

        return queryDiaryMapper.getDiaryDetail(userId, diaryId);
    }

    @Override
    public List<ResponseSharedDiaryListDTO> getSharedDiariesByYearMonth(int userId, int forestId, int year, int month) {

        LocalDateTime startDateTime = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endDateTime = startDateTime
                .withDayOfMonth(startDateTime.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        return queryDiaryMapper.getSharedDiariesByYearMonth(userId, forestId, startDateTime, endDateTime);
    }

    @Override
    public List<ResponseDiaryDetailDTO> getSharedDiariesDetail(int userId, List<Integer> diaryIds) {

        return queryDiaryMapper.getSharedDiariesDetail(userId, diaryIds);
    }

    @Override
    public ResponseDraftedItemsDTO getDraftedItems(int userId, int diaryId) {

        return queryDiaryMapper.getDraftedItems(userId, diaryId);
    }
}
