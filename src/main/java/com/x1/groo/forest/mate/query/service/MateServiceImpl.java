package com.x1.groo.forest.mate.query.service;

import com.x1.groo.forest.mate.query.dao.MateMapper;
import com.x1.groo.forest.mate.query.dto.DiaryByDateDTO;
import com.x1.groo.forest.mate.query.dto.DiaryByMonthDTO;
import com.x1.groo.forest.mate.query.dto.MateForestDetailDTO;
import com.x1.groo.forest.mate.query.dto.MateForestResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MateServiceImpl implements MateService {

    private final MateMapper mateMapper;

    @Override
    public List<DiaryByDateDTO> findDiaries(int userId, int forestId, LocalDate date) {

        // userId가 forestId에 속하는지 확인
        if (!mateMapper.existsUserInForest(userId, forestId)) {
            throw new AccessDeniedException("해당 숲에 대한 접근 권한이 없습니다.");
        }

        // 날짜 범위 생성
        LocalDateTime startDateTime = date.atStartOfDay();      // 00:00:00
        LocalDateTime endDateTime = date.atTime(23, 59, 59);    // 23:59:59

        // 일기 리스트 조회
        return mateMapper.findDiaryByDateAndForestId(forestId, startDateTime, endDateTime);
    }

    @Override
    public List<DiaryByMonthDTO> findDiariesByMonth(int userId, int forestId, int year, int month) {
        if (!mateMapper.existsUserInForest(userId, forestId)) {
            throw new AccessDeniedException("해당 숲에 대한 접근 권한이 없습니다.");
        }

        LocalDateTime startDateTime = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endDateTime = startDateTime
                .withDayOfMonth(startDateTime.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        return mateMapper.findDiariesByMonth(forestId, startDateTime, endDateTime);
    }

    @Override
    public List<MateForestResponseDTO> getForestsByUserId(int userId) {
        return mateMapper.findForestsByUserId(userId);
    }

    @Override
    public MateForestDetailDTO getForestDetail(int forestId) {
        MateForestDetailDTO forestDetails = mateMapper.findForestDetail(forestId);

        if (forestDetails == null) {
            log.error("Forest with id {} not found", forestId);
            throw new EntityNotFoundException("해당 숲을 찾을 수 없습니다: id = " + forestId);
        }


        List<String> nicknames = mateMapper.findNicknamesByForestId(forestId);

        forestDetails.setNicknames(nicknames);

        log.info(forestDetails.toString());
        return forestDetails;
    }

}
