package com.x1.groo.forest.emotion.query.service;

import com.x1.groo.forest.emotion.query.dto.*;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionForestItemDTO;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface QueryForestEmotionService {
    List<QueryForestEmotionForestItemDTO> getPieceOfMemory(int userId, int categoryId, int forestId) throws AccessDeniedException;

    List<QueryForestEmotionMailboxListDTO> getMailboxList(int userId, int forestId);

    List<QueryForestEmotionMailboxDTO> getMailboxDetail(int userId, int id);

    List<QueryForestEmotionDetailDTO> getForestDetail(int userId, int forestId);

    List<QueryForestEmotionListDTO> getForestList(int userId);
}

