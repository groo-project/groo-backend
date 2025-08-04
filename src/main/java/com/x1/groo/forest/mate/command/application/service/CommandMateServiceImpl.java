package com.x1.groo.forest.mate.command.application.service;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.forest.common.domain.aggregate.BackgroundEntity;
import com.x1.groo.forest.common.domain.aggregate.ForestEntity;
import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import com.x1.groo.forest.common.domain.repository.BackgroundRepository;
import com.x1.groo.forest.common.domain.repository.ForestRepository;
import com.x1.groo.forest.common.domain.repository.UserRepository;
import com.x1.groo.forest.mate.command.domain.aggregate.ForestInviteEntity;
import com.x1.groo.forest.mate.command.domain.aggregate.SharedForestEntity;
import com.x1.groo.forest.mate.command.domain.repository.ForestInviteRepository;
import com.x1.groo.forest.mate.command.domain.repository.SharedForestRepository;
import com.x1.groo.forest.mate.command.domain.vo.CreateMateForestRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommandMateServiceImpl implements CommandMateService {


    // 문자열(String)로 key-value를 저장하는 간단한 템플릿
    private final StringRedisTemplate redisTemplate;
    private final SharedForestRepository sharedForestRepository;
    private final ForestRepository forestRepository;
    private final UserRepository userRepository;
    private final BackgroundRepository backgroundRepository;
    private final ForestInviteRepository forestInviteRepository;

    // 우정의 숲 탈퇴
    @Transactional
    @Override
    public void quit(int userId, int forestId) {
        boolean isMember = sharedForestRepository.existsByUserIdAndForestId(userId, forestId);

        if (!isMember) {
            throw new CustomException(ErrorCode.FOREST_ACCESS_DENIED);
        }

        sharedForestRepository.deleteByUserIdAndForestId(userId, forestId);

        // 0명이 될 때 숲 삭제
        int memberCount = sharedForestRepository.countByForestId(forestId);
        if (memberCount == 0) {
            forestRepository.deleteById(forestId);
        }

    }


    // 초대 링크 생성
    @Transactional
    @Override
    public String createInviteLink(int forestId, int userId) {

        if(!forestRepository.existsById(forestId))throw new CustomException(ErrorCode.FOREST_NOT_FOUND);

        for(int i=0; i<3; i++) {
            String inviteCode = UUID.randomUUID().toString().replace("-","").substring(0,16);

            ForestInviteEntity entity = new ForestInviteEntity();
            entity.setForestId(forestId);
            entity.setCode(inviteCode);
            entity.setCreatedBy(userId);
            try {
                forestInviteRepository.saveAndFlush(entity);
                return inviteCode;
            } catch (DataIntegrityViolationException e) {
                if(!isUniqueViolation(e)) throw new CustomException(ErrorCode.FOREST_INVITE_GENERATION_FAILED);
            }
        }
        throw new CustomException(ErrorCode.FOREST_INVITE_CODE_SAVE_FAILED);
    }

    public boolean isUniqueViolation(DataIntegrityViolationException e) {
        String message = String.valueOf(e);
        return message.contains("uk_code") || message.contains("Duplicate") || message.contains("UNIQUE");
    }

    // 초대 수락
    @Transactional
    @Override
    public int acceptInvite(int userId, String inviteCode) {

        String redisKey = "invite:" + inviteCode;
        String value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            throw new CustomException(ErrorCode.FOREST_INVITE_CODE_INVALID);
        }

        int forestId;
        try {
            forestId = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.FOREST_INVITE_CODE_INVALID);
        }

        // 1. 이미 수락했는지 검사
        boolean alreadyJoined = sharedForestRepository.existsByUserIdAndForestId(userId, forestId);
        if (alreadyJoined) {
            throw new CustomException(ErrorCode.FOREST_ALREADY_ACCEPTED_INVITE);
        }

        // 2. 현재 공유숲 참여 인원이 4명 이상인지 검사
        int currentMemberCount = sharedForestRepository.countByForestId(forestId);
        if (currentMemberCount >= 4) {
            throw new CustomException(ErrorCode.FOREST_FULL);
        }

        // 3. 가입
        SharedForestEntity sharedForest = new SharedForestEntity(userId, forestId);
        sharedForestRepository.save(sharedForest);

        // 4. 초대코드 삭제
        redisTemplate.delete(redisKey);

        return forestId;

    }


    // 우정의 숲 생성
    @Override
    @Transactional
    public void createMateForest(int userId, CreateMateForestRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOREST_ACCESS_DENIED));

        BackgroundEntity background = backgroundRepository.findById(1)
                .orElseThrow(() -> new CustomException(ErrorCode.BACKGROUND_NOT_FOUND));

        ForestEntity forest = new ForestEntity();
        forest.setName(request.getForestName());
        forest.setMonth(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        forest.setIsPublic(true);
        forest.setBackground(background);
        forest.setUser(user);

        ForestEntity savedForest = forestRepository.save(forest);

        SharedForestEntity sharedForest = new SharedForestEntity();
        sharedForest.setUserId(user.getId());
        sharedForest.setForestId(savedForest.getId());

        sharedForestRepository.save(sharedForest);
    }
}
