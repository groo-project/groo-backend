package com.x1.groo.forest.mate.query.service;

import com.x1.groo.forest.mate.query.dto.MateForestDetailDTO;
import com.x1.groo.forest.mate.query.dto.MateForestResponseDTO;
import com.x1.groo.forest.mate.query.dto.MateItemDTO;

import java.util.List;

public interface MateService {

    List<MateForestResponseDTO> getForestsByUserId(int userId);

    MateForestDetailDTO getForestDetail(int forestId);

    List<MateItemDTO> getPieceOfMemory(int userId, int categoryId, int forestId);
}