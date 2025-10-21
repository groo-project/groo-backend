package com.x1.groo.forest.emotion.command.application.service;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.forest.common.domain.aggregate.*;
import com.x1.groo.common.sse.SseEventPublisher;
import com.x1.groo.common.sse.SseEventType;
import com.x1.groo.common.sse.payload.ForestUpdatedPayload;
import com.x1.groo.common.sse.payload.ItemPlacedPayload;
import com.x1.groo.forest.common.domain.aggregate.BackgroundEntity;
import com.x1.groo.forest.common.domain.aggregate.ForestEntity;
import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import com.x1.groo.forest.common.domain.repository.*;
import com.x1.groo.forest.emotion.command.domain.aggregate.*;
import com.x1.groo.forest.emotion.command.domain.repository.*;
import com.x1.groo.forest.emotion.command.domain.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommandEmotionForestServiceImpl implements CommandEmotionForestService {

    private final PlacementRepository placementRepository;
    private final ForestItemRepository forestItemRepository;
    private final ForestRepository forestRepository;
    private final UserRepository userRepository;
    private final MailboxRepository mailboxRepository;
    private final BackgroundRepository backgroundRepository;
    private final SseEventPublisher sseEventPublisher;

    /* 아이템 회수 */
    @Transactional
    @Override
    public void retrieveItemsByIds(int userId, List<Integer> placementIds) {

        Integer forestId = null;

        for (Integer placementId : placementIds) {
            PlacementEntity placement = placementRepository.findById(placementId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PLACEMENT_NOT_FOUND));

            if (forestId == null) {
                forestId = placement.getForestItem().getForest().getId();
            }

            ForestItemEntity forestItem = forestItemRepository.findById(placement.getForestItem().getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PLACEMENT_NOT_FOUND));

            // 배치 개수 감소
            forestItem.decreasePlacedCount();
            forestItemRepository.save(forestItem);

            // 배치 삭제
            placementRepository.deleteById(placementId);
        }

        if (forestId != null) {
            // 브로드캐스트
            sseEventPublisher.publish(forestId, SseEventType.ITEM_PLACED,new ItemPlacedPayload(userId, forestId));
        }
    }

    /* 전체 아이템 회수 */
    @Transactional
    @Override
    public void retrieveAllItems(int userId, int forestId) {
        // 1. forestId + userId로 user_item 조회
        List<ForestItemEntity> userItems = forestItemRepository.findByForestId(forestId);

        if (userItems.isEmpty()) {
            return; // 조회된 게 없으면 끝
        }

        // 2. placed_count를 0으로 변경
        for (ForestItemEntity userItem : userItems) {
            userItem.setPlacedCount(0);
        }
        forestItemRepository.saveAll(userItems);

        // 3. forest_item id 목록 가져오기
        List<Integer> forestItemIds = userItems.stream()
                .map(ForestItemEntity::getId)
                .collect(Collectors.toList());

        // 4. placement 삭제
        placementRepository.deleteByForestItemIdIn(forestItemIds);
    }

    /* 아이템 배치 */
    @Transactional
    @Override
    public void placeItem(int userId, RequestPlacementVO requestPlacementVO) {
        int itemId = requestPlacementVO.getItemId();
        int forestId = requestPlacementVO.getForestId();

        // 1. 기존 forestItem 조회
        Optional<ForestItemEntity> optionalForestItem = forestItemRepository
                .findByItemIdAndForestId(itemId, forestId);

        ForestItemEntity forestItem;

        if (optionalForestItem.isPresent()) {
            // 2-1. 이미 있다면 placed_count 증가
            forestItem = optionalForestItem.get();
            forestItem.incrementPlacedCount();
            forestItem.incrementTotalCount();
        } else {
            // 2-2. 없다면 새로 생성
            ForestEntity forest = forestRepository.findById(forestId)
                    .orElseThrow(() -> new CustomException(ErrorCode.FOREST_NOT_FOUND));

            forestItem = new ForestItemEntity();
            forestItem.setItemId(itemId);
            forestItem.setForest(forest);
            forestItem.setTotalCount(1);
            forestItem.setPlacedCount(1);

            forestItemRepository.save(forestItem);
        }

        // 3. placement 저장
        PlacementEntity placement = new PlacementEntity();
        placement.setPositionX(requestPlacementVO.getItemPositionX());
        placement.setPositionY(requestPlacementVO.getItemPositionY());
        placement.setForestItem(forestItem);
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

        Integer forestId = null;

        for (RequestReplacementVO vo : replacementVOList) {
            if (!vo.isValid()) {
                throw new CustomException(ErrorCode.PLACEMENT_NOT_FOUND);
            }

            // 1. placementId로 PlacementEntity 조회
            PlacementEntity placement = placementRepository.findById(vo.getPlacementId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PLACEMENT_NOT_FOUND));

            if (forestId == null) {
                forestId = placement.getForestItem().getForest().getId();
            }

            // 3. 위치/크기/Z-Index 변경
            placement.setPositionX(vo.getItemPositionX());
            placement.setPositionY(vo.getItemPositionY());
            placement.setWidth(vo.getItemWidth());
            placement.setHeight(vo.getItemHeight());
            placement.setZIndex(vo.getItemZIndex());
        }
        // JPA의 dirty checking으로 트랜잭션 종료 시 자동 업데이트

        if (forestId != null) {
            // 브로드캐스트
            sseEventPublisher.publish(forestId, SseEventType.ITEM_PLACED,new ItemPlacedPayload(userId, forestId));
        }
    }
    
    /* 보관된 아이템 배치 */
    @Transactional
    @Override
    public void placeStoredItem(int userId, RequestReplantVO requestReplantVO) {
        // 1. forestItemId로 ForestItemEntity 조회
        ForestItemEntity forestItem = forestItemRepository.findById(requestReplantVO.getForestItemId())
                .orElseThrow(() -> new CustomException(ErrorCode.PLACEMENT_NOT_FOUND));

        // 2. placement 생성
        PlacementEntity placement = new PlacementEntity();
        placement.setPositionX(requestReplantVO.getItemPositionX());
        placement.setPositionY(requestReplantVO.getItemPositionY());
        placement.setWidth(requestReplantVO.getItemWidth());
        placement.setHeight(requestReplantVO.getItemHeight());
        placement.setZIndex(requestReplantVO.getItemZIndex());
        placement.setForestItem(forestItem);

        placementRepository.save(placement);

        // 4. userItem의 placed_count 증가
        forestItem.incrementPlacedCount();

        int forestId = requestReplantVO.getForestId();

        // 브로드캐스트
        sseEventPublisher.publish(forestId, SseEventType.ITEM_PLACED,new ItemPlacedPayload(userId, forestId));
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
    @Transactional
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
        forest.setCreatedAt(LocalDateTime.now());
        forest.setIsPublic(false);
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
