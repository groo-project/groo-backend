package com.x1.groo.forest.emotion.query.service;

import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionDetailDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionListDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionMailboxDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionMailboxListDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionUserItemDTO;
import com.x1.groo.forest.emotion.query.dto.*;
import com.x1.groo.forest.emotion.query.repository.QueryForestEmotionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueryForestEmotionServiceImpl implements QueryForestEmotionService {

    @Autowired
    private QueryForestEmotionMapper queryForestEmotionMapper;

    // 사용자가 보유한 기억의 조각 카테고리별 조회
    public List<QueryForestEmotionUserItemDTO> getPieceOfMemory(int userId, int categoryId, int forestId) {
        int forestOwnerId = queryForestEmotionMapper.findUserIdByForestId(forestId);

        if (forestOwnerId != userId) {
            throw new AccessDeniedException("해당 유저는 forest에 접근할 수 없습니다.");
        }

        return queryForestEmotionMapper.findPieceOfMemory(userId, categoryId, forestId);
    }

    public List<QueryForestEmotionMailboxListDTO> getMailboxList(int userId, int forestId) {
        return queryForestEmotionMapper.findMailboxList(userId, forestId);
    }

    public List<QueryForestEmotionMailboxDTO> getMailboxDetail(int userId, int id) {
        return queryForestEmotionMapper.findMailboxDetail(userId, id);
    }

    public List<QueryForestEmotionDetailDTO> getForestDetail(int userId, int forestId) {
        return queryForestEmotionMapper.findForestDetail(userId, forestId);
    }

    // 날짜별 일기 조회
    @Override
    public List<QueryForestEmotionDiaryByDateDTO> findDiaries(int userId, int forestId, LocalDate date) {

        boolean isOwner = queryForestEmotionMapper.isOwnerOfForest(userId, forestId);
        boolean isShared = queryForestEmotionMapper.existsUserInForest(userId, forestId);

        if (!(isOwner || isShared)) {
            throw new AccessDeniedException("해당 숲에 대한 접근 권한이 없습니다.");
        }

        // 날짜 범위 생성
        LocalDateTime startDateTime = date.atStartOfDay();      // 00:00:00
        LocalDateTime endDateTime = date.atTime(23, 59, 59);    // 23:59:59

        // 일기 리스트 조회
        return queryForestEmotionMapper.findDiaryByDateAndForestId(forestId, startDateTime, endDateTime);
    }

    // 월 별 일기 조회
    @Override
    public List<QueryForestEmotionDiaryByMonthDTO> findDiariesByMonth(int userId, int forestId, int year, int month) {
        boolean isOwner = queryForestEmotionMapper.isOwnerOfForest(userId, forestId);
        boolean isShared = queryForestEmotionMapper.existsUserInForest(userId, forestId);

        if (!(isOwner || isShared)) {
            throw new AccessDeniedException("해당 숲에 대한 접근 권한이 없습니다.");
        }

        LocalDateTime startDateTime = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endDateTime = startDateTime
                .withDayOfMonth(startDateTime.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        return queryForestEmotionMapper.findDiariesByMonth(forestId, startDateTime, endDateTime);
    }

    public List<QueryForestEmotionListDTO> getForestList(int userId) {
        return queryForestEmotionMapper.findForestList(userId);
    }

}
