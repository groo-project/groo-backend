package com.x1.groo.common.sse.service;

import com.x1.groo.forest.mate.command.domain.repository.SharedForestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MateSseService {

    private final SharedForestRepository sharedForestRepository;

    @Transactional(readOnly = true)
    public boolean checkForestAccess(int userId, int forestId) {
        return sharedForestRepository.existsByUserIdAndForestId(userId, forestId);
    }
}
