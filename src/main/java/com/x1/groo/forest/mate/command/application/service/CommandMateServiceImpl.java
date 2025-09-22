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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.x1.groo.common.sse.SseEventType.USER_JOINED;
import static com.x1.groo.forest.mate.command.domain.aggregate.InviteCodeStatus.REVOKED;
import static com.x1.groo.forest.mate.command.domain.aggregate.InviteCodeStatus.USED;

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

        // 0명이 될 때 숲 삭제
        int memberCount = sharedForestRepository.countByForestId(forestId);
        if (memberCount == 0) {
            forestRepository.deleteById(forestId);
        }

        // 브로드캐스트
        sseEventPublisher.publish(forestId, SseEventType.USER_LEFT,new UserLeftPayload(userId, forestId));
    }


    // 초대 링크 생성
    @Transactional
    @Override
    public String createInviteLink(int forestId, int userId) {

        if(!forestRepository.existsById(forestId))throw new CustomException(ErrorCode.FOREST_NOT_FOUND);

        for(int i=0; i<3; i++) {
            String inviteCode = UUID.randomUUID().toString().replace("-","").substring(0,8);

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


        ForestInviteEntity invite = forestInviteRepository.findByCode(inviteCode);
        if(invite == null) throw new CustomException(ErrorCode.FOREST_INVITE_CODE_INVALID);

        int forestId = invite.getForestId();

        if (invite.getStatus() == REVOKED)
            throw new CustomException(ErrorCode.FOREST_INVITE_CODE_REVOKED);
        if (invite.getStatus() == USED)
            throw new CustomException(ErrorCode.FOREST_INVITE_CODE_USED);
        if (LocalDateTime.now().isAfter(invite.getExpiresAt()))
            throw new CustomException(ErrorCode.FOREST_INVITE_CODE_EXPIRED);

        // 초대코드 동시 수락 방지
        int updated = forestInviteRepository.consume(inviteCode);
        if (updated != 1) {
            throw new CustomException(ErrorCode.FOREST_INVITE_CODE_INVALID);
        }

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

//        Optional<UserEntity> user = userRepository.findById(userId);

        UserEntity  user = userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String nickname = user.getNickname();

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
        log.info("이미 참여한 사용자가 아닙니다." );

        // DB 갱신 (예: 매핑 추가)
        sharedForestRepository.save(new SharedForestEntity(userId, forestId));

        // 커밋 "후"에만 전송되도록 예약 (롤백되면 전송 안 함)
        sseEventPublisher.publishAfterCommit(
                forestId,
                USER_JOINED,
                new UserJoinedPayload(userId, nickname) // payload는 작고 명확하게
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
