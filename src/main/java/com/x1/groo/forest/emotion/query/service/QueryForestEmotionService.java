package com.x1.groo.forest.emotion.query.service;

import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionDetailDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionListDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionMailboxDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionMailboxListDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionUserItemDTO;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface QueryForestEmotionService {
    List<QueryForestEmotionUserItemDTO> getPieceOfMemory(int userId, int categoryId, int forestId) throws AccessDeniedException;

    List<QueryForestEmotionMailboxListDTO> getMailboxList(int userId, int forestId);

    List<QueryForestEmotionMailboxDTO> getMailboxDetail(int userId, int id);

    List<QueryForestEmotionDetailDTO> getForestDetail(int userId, int forestId);

    List<QueryForestEmotionListDTO> getForestList(int userId);
}

