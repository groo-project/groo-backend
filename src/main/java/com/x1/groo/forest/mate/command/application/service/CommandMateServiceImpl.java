package com.x1.groo.forest.mate.command.application.service;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.common.sse.SseEventPublisher;
import com.x1.groo.common.sse.payload.UserJoinedPayload;
import com.x1.groo.common.sse.payload.UserLeftPayload;
import com.x1.groo.forest.common.domain.aggregate.BackgroundEntity;
import com.x1.groo.forest.common.domain.aggregate.ForestEntity;
import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import com.x1.groo.forest.common.domain.repository.BackgroundRepository;
import com.x1.groo.forest.common.domain.repository.ForestRepository;
import com.x1.groo.forest.common.domain.repository.UserRepository;
import com.x1.groo.forest.mate.command.domain.aggregate.ForestInviteEntity;
import com.x1.groo.forest.mate.command.domain.aggregate.SharedForestEntity;
import com.x1.groo.common.sse.SseEventType;
import com.x1.groo.forest.mate.command.domain.repository.ForestInviteRepository;
import com.x1.groo.forest.mate.command.domain.repository.SharedForestRepository;
import com.x1.groo.forest.mate.command.domain.vo.CreateMateForestRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.Now;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.x1.groo.common.sse.SseEventType.USER_JOINED;
import static com.x1.groo.forest.mate.command.domain.aggregate.InviteCodeStatus.REVOKED;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandMateServiceImpl implements CommandMateService {

    private final SharedForestRepository sharedForestRepository;
    private final ForestRepository forestRepository;
    private final UserRepository userRepository;
    private final BackgroundRepository backgroundRepository;
    private final ForestInviteRepository forestInviteRepository;
    private final SseEventPublisher sseEventPublisher;

    // 우정의 숲 탈퇴
    @Transactional
    @Override
    public void quit(int userId, int forestId) {
        boolean isMember = sharedForestRepository.existsByUserIdAndForestId(userId, forestId);

        if (!isMember) {
            throw new CustomException(ErrorCode.FOREST_ACCESS_DENIED);
        }

        sharedForestRepository.deleteByUserIdAndForestId(userId, forestId);

        ForestEntity forest = forestRepository.findById(forestId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOREST_NOT_FOUND));

        // 숲 회원 조회
        List<SharedForestEntity> member = sharedForestRepository.findAllByForestIdOrderByIdAsc(forestId);


        boolean deleted = false;

        // 우정의 숲 주인일 경우
        if( userId == forest.getUser().getId()) {
            // 숲 주인 제외 필터링
            List<SharedForestEntity> remaining = member.stream()
                    .filter(u -> u.getUserId() != userId)
                    .toList();

            if(!remaining.isEmpty()) {
                SharedForestEntity newOwnerMember = remaining.get(0);
                UserEntity user = userRepository.findById(newOwnerMember.getUserId())
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                forest.setUser(user);
                forestRepository.save(forest);
            } else {
                forestRepository.deleteById(forestId);
                deleted = true;
            }
        }

        // 0명이 될 때 숲 삭제
        if(!deleted) {
            int memberCount = sharedForestRepository.countByForestId(forestId);
            if (memberCount == 0) {
                forestRepository.deleteById(forestId);
            }
        }

        // 브로드캐스트
        sseEventPublisher.publish(forestId, SseEventType.USER_LEFT,new UserLeftPayload(userId, forestId));
    }


    // 초대 링크 생성
    @Transactional
    @Override
    public String createInviteLink(int forestId, int userId) {

        if(!forestRepository.existsById(forestId)) {
            throw new CustomException(ErrorCode.FOREST_NOT_FOUND);
        }

        ForestInviteEntity invite = forestInviteRepository.findByForestId(forestId).orElse(null);

        // 초대 링크 존재
        if(invite != null) {
            if(invite.getExpiresAt().isBefore(LocalDateTime.now())) { // 초대 링크 만료 시
                invite.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24시간 연장
                forestInviteRepository.save(invite);
            }
            return invite.getCode();
        }

        String inviteCode = UUID.randomUUID().toString().replace("-","").substring(0,16);

        ForestInviteEntity entity = new ForestInviteEntity();
        entity.setForestId(forestId);
        entity.setCode(inviteCode);
        entity.setCreatedBy(userId);

        try {
            forestInviteRepository.saveAndFlush(entity);
            return inviteCode;
        } catch (DataIntegrityViolationException e) {
            if(isUniqueViolation(e)) {
                throw new CustomException(ErrorCode.FOREST_INVITE_UNIQUE_VIOLATION);
            }
            throw new CustomException(ErrorCode.FOREST_INVITE_GENERATION_FAILED);
        }

    }

    public boolean isUniqueViolation(DataIntegrityViolationException e) {
        String message = String.valueOf(e);
        return message.contains("uk_code") || message.contains("Duplicate") || message.contains("UNIQUE");
    }

    // 초대 수락
    @Transactional
    @Override
    public int acceptInvite(int userId, String inviteCode) {

        UserEntity  user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String nickname = user.getNickname();

        ForestInviteEntity invite = forestInviteRepository.findByCode(inviteCode);

        if(invite == null) throw new CustomException(ErrorCode.FOREST_INVITE_CODE_INVALID);

        int forestId = invite.getForestId();

        if (invite.getStatus() == REVOKED)
            throw new CustomException(ErrorCode.FOREST_INVITE_CODE_REVOKED);

        if (LocalDateTime.now().isAfter(invite.getExpiresAt()))
            throw new CustomException(ErrorCode.FOREST_INVITE_CODE_EXPIRED);


        if (sharedForestRepository.existsByUserIdAndForestId(userId, forestId)) {
            throw new CustomException(ErrorCode.FOREST_ALREADY_ACCEPTED_INVITE);
        }

//        ForestEntity lockedForest = forestRepository.lockById(forestId); // 여기서 잠금
        int count = sharedForestRepository.countByForestIdForUpdate(forestId);
        if (count >= 4) {
            throw new CustomException(ErrorCode.FOREST_FULL);
        }

        // 가입
        SharedForestEntity sharedForest = new SharedForestEntity(userId, forestId);
        sharedForestRepository.save(sharedForest);


        // 브로드캐스트
        sseEventPublisher.publish(forestId, USER_JOINED,new UserJoinedPayload(userId, nickname));

        return forestId;

    }

    @Transactional
    @Override
    public void joinForest(int userId, int forestId, String nickname) {
        if (sharedForestRepository.existsByUserIdAndForestId(userId, forestId)) {
            return; // 이미 참가
        }

        sharedForestRepository.save(new SharedForestEntity(userId, forestId));

        // 커밋 "후"에만 전송되도록 예약 (롤백되면 전송 안 함)
        sseEventPublisher.publishAfterCommit(
                forestId,
                USER_JOINED,
                new UserJoinedPayload(userId, nickname)
        );
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
        forest.setCreatedAt(LocalDateTime.now());
        forest.setIsPublic(false);
        forest.setBackground(background);
        forest.setUser(user);

        ForestEntity savedForest = forestRepository.save(forest);

        SharedForestEntity sharedForest = new SharedForestEntity();
        sharedForest.setUserId(user.getId());
        sharedForest.setForestId(savedForest.getId());

        sharedForestRepository.save(sharedForest);
    }
}
