package com.x1.groo.forest.mate.query.service;

import com.x1.groo.forest.mate.query.dto.DiaryByDateDTO;
import com.x1.groo.forest.mate.query.dto.DiaryByMonthDTO;
import com.x1.groo.forest.mate.query.dto.MateForestDetailDTO;
import com.x1.groo.forest.mate.query.dto.MateForestResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface MateService {
    List<DiaryByDateDTO> findDiaries(int userId, int forestId, LocalDate date);

    List<DiaryByMonthDTO> findDiariesByMonth(int userId, int forestId, int year, int month);

    List<MateForestResponseDTO> getForestsByUserId(int userId);

    MateForestDetailDTO getForestDetail(int forestId);
}