package com.x1.groo.forest.emotion.query.service;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.forest.emotion.query.dto.*;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionForestItemDTO;
import com.x1.groo.forest.emotion.query.repository.QueryForestEmotionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryForestEmotionServiceImpl implements QueryForestEmotionService {

    @Autowired
    private QueryForestEmotionMapper queryForestEmotionMapper;

    // 사용자가 보유한 기억의 조각 카테고리별 조회
    public List<QueryForestEmotionForestItemDTO> getPieceOfMemory(int userId, int categoryId, int forestId) {
        int forestOwnerId = queryForestEmotionMapper.findUserIdByForestId(forestId);

        if (forestOwnerId != userId) {
            throw new CustomException(ErrorCode.FOREST_ACCESS_DENIED);
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
        List<QueryForestEmotionDetailDTO> result = queryForestEmotionMapper.findForestDetail(userId, forestId);

        if (result.isEmpty()) {
            throw new CustomException(ErrorCode.FOREST_ACCESS_DENIED);
        }
        return result;
    }

    public List<QueryForestEmotionListDTO> getForestList(int userId) {
        return queryForestEmotionMapper.findForestList(userId);
    }

}
