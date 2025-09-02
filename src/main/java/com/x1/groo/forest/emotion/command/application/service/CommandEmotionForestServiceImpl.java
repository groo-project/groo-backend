package com.x1.groo.forest.emotion.command.application.service;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.common.sse.SseEventPublisher;
import com.x1.groo.common.sse.SseEventType;
import com.x1.groo.common.sse.payload.ForestUpdatedPayload;
import com.x1.groo.common.sse.payload.ItemPlacedPayload;
import com.x1.groo.common.sse.payload.UserLeftPayload;
import com.x1.groo.forest.common.domain.aggregate.BackgroundEntity;
import com.x1.groo.forest.common.domain.aggregate.ForestEntity;
import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import com.x1.groo.forest.common.domain.repository.BackgroundRepository;
import com.x1.groo.forest.common.domain.repository.ForestRepository;
import com.x1.groo.forest.common.domain.repository.UserRepository;
import com.x1.groo.forest.emotion.command.domain.aggregate.*;
import com.x1.groo.forest.emotion.command.domain.repository.*;
import com.x1.groo.forest.emotion.command.domain.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommandEmotionForestServiceImpl implements CommandEmotionForestService {

    private final PlacementRepository placementRepository;
    private final UserItemRepository userItemRepository;
    private final ForestRepository forestRepository;
    private final UserRepository userRepository;
    private final MailboxRepository mailboxRepository;
    private final BackgroundRepository backgroundRepository;
    private final SseEventPublisher sseEventPublisher;

    /* 아이템 회수 */
    @Transactional
    @Override
    public void retrieveItemsByIds(int userId, List<Integer> placementIds) {

        for (Integer placementId : placementIds) {
            PlacementEntity placement = placementRepository.findById(placementId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PLACEMENT_NOT_FOUND));

            // userId 검증
            if (placement.getUser().getId() != userId) {
                throw new CustomException(ErrorCode.PLACEMENT_ACCESS_DENIED);
            }

            UserItemEntity userItem = userItemRepository.findById(placement.getUserItem().getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PLACEMENT_NOT_FOUND));

            // 배치 개수 감소
            userItem.decreasePlacedCount();
            userItemRepository.save(userItem);

            // 배치 삭제
            placementRepository.deleteById(placementId);
        }
    }

    /* 전체 아이템 회수 */
    @Transactional
    @Override
    public void retrieveAllItems(int userId, int forestId) {
        // 1. forestId + userId로 user_item 조회
        List<UserItemEntity> userItems = userItemRepository.findByUserIdAndForestId(userId, forestId);

        if (userItems.isEmpty()) {
            return; // 조회된 게 없으면 끝
        }

        // 2. placed_count를 0으로 변경
        for (UserItemEntity userItem : userItems) {
            userItem.setPlacedCount(0);
        }
        userItemRepository.saveAll(userItems);

        // 3. user_item id 목록 가져오기
        List<Integer> userItemIds = userItems.stream()
                .map(UserItemEntity::getId)
                .collect(Collectors.toList());

        // 4. placement 삭제
        placementRepository.deleteByUserItemIdIn(userItemIds);
    }

    /* 아이템 배치 */
    @Transactional
    @Override
    public void placeItem(int userId, RequestPlacementVO requestPlacementVO) {
        int itemId = requestPlacementVO.getItemId();
        int forestId = requestPlacementVO.getForestId();

        // 1. 기존 userItem 조회
        Optional<UserItemEntity> optionalUserItem = userItemRepository
                .findByUserIdAndItemIdAndForestId(userId, itemId, forestId);

        UserItemEntity userItem;

        if (optionalUserItem.isPresent()) {
            // 2-1. 이미 있다면 placed_count 증가
            userItem = optionalUserItem.get();
            userItem.incrementPlacedCount();
            userItem.incrementTotalCount();
        } else {
            // 2-2. 없다면 새로 생성
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.FOREST_ACCESS_DENIED));
            ForestEntity forest = forestRepository.findById(forestId)
                    .orElseThrow(() -> new CustomException(ErrorCode.FOREST_NOT_FOUND));

            userItem = new UserItemEntity();
            userItem.setUser(user);
            userItem.setItemId(itemId);
            userItem.setForest(forest);
            userItem.setTotalCount(1);
            userItem.setPlacedCount(1);

            userItemRepository.save(userItem);
        }

        // 3. placement 저장
        PlacementEntity placement = new PlacementEntity();
        placement.setPositionX(requestPlacementVO.getItemPositionX());
        placement.setPositionY(requestPlacementVO.getItemPositionY());
        placement.setUser(userItem.getUser()); // 또는 userRepository에서 다시 가져와도 됨
        placement.setUserItem(userItem);
        placement.setHeight(requestPlacementVO.getItemHeight());
        placement.setWidth(requestPlacementVO.getItemWidth());
        placement.setZIndex(requestPlacementVO.getItemZIndex());

        // 브로드캐스트
        sseEventPublisher.publish(forestId, SseEventType.ITEM_PLACED,new ItemPlacedPayload(userId, forestId));

        placementRepository.save(placement);
    }

    /* 배치된 아이템 재배치 */
    @Transactional
    @Override
    public void replaceItem(int userId, List<RequestReplacementVO> replacementVOList) {
        if (replacementVOList == null || replacementVOList.isEmpty()) {
            throw new CustomException(ErrorCode.PLACEMENT_NOT_FOUND);
        }

        for (RequestReplacementVO vo : replacementVOList) {
            if (!vo.isValid()) {
                throw new CustomException(ErrorCode.PLACEMENT_NOT_FOUND);
            }

            // 1. placementId로 PlacementEntity 조회
            PlacementEntity placement = placementRepository.findById(vo.getPlacementId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PLACEMENT_NOT_FOUND));

            // 2. 소유자 검증
            if (placement.getUser() == null || placement.getUser().getId() != userId) {
                throw new CustomException(ErrorCode.PLACEMENT_ACCESS_DENIED);
            }

            // 3. 위치/크기/Z-Index 변경
            placement.setPositionX(vo.getItemPositionX());
            placement.setPositionY(vo.getItemPositionY());
            placement.setWidth(vo.getItemWidth());
            placement.setHeight(vo.getItemHeight());
            placement.setZIndex(vo.getItemZIndex());
        }
        // JPA의 dirty checking으로 트랜잭션 종료 시 자동 업데이트
    }
    
    /* 회수 혹은 보관된 아이템 배치 */
    @Transactional
    @Override
    public void placeStoredItem(int userId, RequestReplantVO requestReplantVO) {
        // 1. userItemId로 UserItemEntity 조회
        UserItemEntity userItem = userItemRepository.findById(requestReplantVO.getUserItemId())
                .orElseThrow(() -> new CustomException(ErrorCode.PLACEMENT_NOT_FOUND));

        // 2. 소유자 검증
        if (userItem.getUser() == null || !Objects.equals(userItem.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.PLACEMENT_ACCESS_DENIED);
        }

        // 3. placement 생성
        PlacementEntity placement = new PlacementEntity();
        placement.setPositionX(requestReplantVO.getItemPositionX());
        placement.setPositionY(requestReplantVO.getItemPositionY());
        placement.setWidth(requestReplantVO.getItemWidth());
        placement.setHeight(requestReplantVO.getItemHeight());
        placement.setZIndex(requestReplantVO.getItemZIndex());
        placement.setUser(userItem.getUser());
        placement.setUserItem(userItem);

        placementRepository.save(placement);

        // 4. userItem의 placed_count 증가
        userItem.incrementPlacedCount();
    }

    /* 방명록 작성 */
    @Override
    public void createMailbox(int userId, RequestMailboxVO requestMailboxVO) {
        MailboxEntity mailbox = new MailboxEntity();
        mailbox.setContent(requestMailboxVO.getContent());
        mailbox.setUserId(userId);
        mailbox.setForestId(requestMailboxVO.getForestId());
        mailbox.setIsDeleted(false);
        mailbox.setCreatedAt(LocalDateTime.now());

        mailboxRepository.save(mailbox);
    }

    /* 방명록 삭제 */
    @Transactional
    @Override
    public void deleteMailbox(int userId, int mailboxId, int forestId) {
        boolean isOwner = forestRepository.existsByIdAndUserId(forestId, userId);
        if (!isOwner) {
            throw new CustomException(ErrorCode.MAILBOX_ACCESS_DENIED);
        }

        mailboxRepository.softDeleteById(mailboxId);
    }

    // 숲의 공개 여부 변경
    @Override
    public void updateForestPublic(int forestId, int userId) {
        ForestEntity forest = forestRepository.findById(forestId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOREST_NOT_FOUND));

        // 여기서 꼭 본인 확인
        if (forest.getUser().getId() != userId) {
            throw new CustomException(ErrorCode.FOREST_ACCESS_DENIED);
        }

        // 숲의 공개 여부 토글 (true -> false, false -> true)
        boolean currentPublicStatus = forest.getIsPublic();
        forest.setIsPublic(!currentPublicStatus);

        // 숲 정보 저장
        forestRepository.save(forest);
    }

    // 감정의 숲 생성
    @Override
    @Transactional
    public void createEmotionForest(int userId, RequestCreateVO request) {
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

        forestRepository.save(forest);
    }

    // 숲 이름 수정하기
    @Transactional
    @Override
    public void updateForestName(int forestId, int userId, String newName) {
        ForestEntity forest = forestRepository.findById(forestId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOREST_NOT_FOUND));

        // 공유된 숲 참여 여부 확인
        boolean isMember = forest.getUser().getId() == userId ||
                forestRepository.isUserInSharedForest(userId, forestId);

        if (!isMember) {
            throw new CustomException(ErrorCode.FOREST_ACCESS_DENIED);
        }

        forest.setName(newName);
        forestRepository.save(forest);

        // 브로드캐스트
        sseEventPublisher.publish(forestId, SseEventType.FOREST_UPDATED, new ForestUpdatedPayload(userId, forestId, newName));
    }
}
