package com.x1.groo.forest.mate.command.application.service;

import com.x1.groo.forest.common.domain.aggregate.BackgroundEntity;
import com.x1.groo.forest.common.domain.aggregate.ForestEntity;
import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import com.x1.groo.forest.common.domain.repository.BackgroundRepository;
import com.x1.groo.forest.common.domain.repository.ForestRepository;
import com.x1.groo.forest.common.domain.repository.UserRepository;
import com.x1.groo.forest.mate.command.domain.aggregate.SharedForestEntity;
import com.x1.groo.forest.mate.command.domain.repository.SharedForestRepository;
import com.x1.groo.forest.mate.command.domain.vo.CreateMateForestRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
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

    // 우정의 숲 탈퇴
    @Override
    public void quit(int userId, int forestId) {
        boolean isMember = sharedForestRepository.existsByUserIdAndForestId(userId, forestId);

        if (!isMember) {
            throw new IllegalStateException("해당 숲에 속해있지 않습니다.");
        }

        sharedForestRepository.deleteByUserIdAndForestId(userId, forestId);

        // 0명이 될 때 숲 삭제
        int memberCount = sharedForestRepository.countByForestId(forestId);
        if (memberCount == 0) {
            forestRepository.deleteById(forestId);
        }

    }

    // 초대 링크 생성
    @Override
    public String createInviteLink(int forestId) {


        // 초대 링크 생성 로직 작성
        // UUID 기반 inviteCode 생성
        String inviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // Redis에 저장
        String redisKey = "invite:" + inviteCode;
        String redisValue = String.valueOf(forestId);

        redisTemplate.opsForValue()
                .set(redisKey, redisValue, Duration.ofHours(24));

        return inviteCode;
    }

    // 초대 수락
    @Transactional
    @Override
    public int acceptInvite(int userId, String inviteCode) {

        String redisKey = "invite:" + inviteCode;
        String value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            throw new IllegalArgumentException("초대코드가 만료되었거나 유효하지 않습니다.");
        }

        int forestId;
        try {
            forestId = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("초대 코드에 해당하는 데이터가 잘못되었습니다.");
        }

        // 1. 이미 수락했는지 검사
        boolean alreadyJoined = sharedForestRepository.existsByUserIdAndForestId(userId, forestId);
        if (alreadyJoined) {
            throw new IllegalStateException("이미 이 숲에 참여한 사용자입니다.");
        }

        // 2. 현재 공유숲 참여 인원이 4명 이상인지 검사
        int currentMemberCount = sharedForestRepository.countByForestId(forestId);
        if (currentMemberCount >= 4) {
            throw new IllegalStateException("이 공유숲은 정원이 가득 찼습니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        BackgroundEntity background = backgroundRepository.findById(1)
                .orElseThrow(() -> new IllegalArgumentException("기본 배경을 찾을 수 없습니다."));

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
