package com.x1.groo.forest.mate.query.service;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.forest.emotion.query.dto.PlacementDTO;
import com.x1.groo.forest.mate.query.dao.MateMapper;
import com.x1.groo.forest.mate.query.dto.MateForestDetailDTO;
import com.x1.groo.forest.mate.query.dto.MateForestResponseDTO;
import com.x1.groo.forest.mate.query.dto.MateItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MateServiceImpl implements MateService {

    private final MateMapper mateMapper;

    @Override
    public List<MateForestResponseDTO> getForestsByUserId(int userId) {
        return mateMapper.findForestsByUserId(userId);
    }

    @Override
    public MateForestDetailDTO getForestDetail(int forestId) {
        MateForestDetailDTO forestDetails = mateMapper.findForestBaseDetail(forestId);

        if (forestDetails == null) {
            throw new CustomException(ErrorCode.FOREST_NOT_FOUND);
        }

        List<String> nicknames = mateMapper.findNicknamesByForestId(forestId);
        forestDetails.setNicknames(nicknames);

        List<PlacementDTO> placementList = mateMapper.findPlacementListByForestId(forestId);
        forestDetails.setPlacementList(placementList);

        int writtenDiaryCount = mateMapper.getWrittenDiaryCountByForestId(forestId);
        forestDetails.setWrittenDiaryCount(writtenDiaryCount);

        return forestDetails;
    }

    @Override
    public List<MateItemDTO> getPieceOfMemory(int userId, int categoryId, int forestId) {
        return mateMapper.getPieceOfMemory(userId, categoryId, forestId);
    }
}
