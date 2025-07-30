package com.x1.groo.item.service;

import com.x1.groo.forest.common.domain.repository.ForestRepository;
import com.x1.groo.forest.emotion.command.domain.repository.EmotionSharedForestRepository;
import com.x1.groo.item.domain.storage.aggregate.UserItemStorageEntity;
import com.x1.groo.item.domain.storage.repository.UserItemStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class UserItemStorageServiceImpl implements UserItemStorageService {

    private final UserItemStorageRepository userItemStorageRepo;
    private final ForestRepository forestRepo;
    private final EmotionSharedForestRepository sharedForestRepo;

    @Autowired
    public UserItemStorageServiceImpl(UserItemStorageRepository userItemStorageRepository,
                                      ForestRepository forestRepository,
                                      EmotionSharedForestRepository sharedForestRepository) {
        this.userItemStorageRepo = userItemStorageRepository;
        this.forestRepo = forestRepository;
        this.sharedForestRepo = sharedForestRepository;

    }

    @Override
    public void saveItemToStorage(int userId, int itemId, int forestId) {

        //  1. 권한 체크 (개인숲 소유자 or 공유숲 사용자)
        boolean isOwner = forestRepo.findById(forestId)
                .map(forest -> forest.getUser().getId() == userId)
                .orElse(false);

        boolean hasSharedAccess = sharedForestRepo.existsByUserIdAndForestId(userId, forestId);

        if (!(isOwner || hasSharedAccess)) {
            throw new AccessDeniedException("이 숲에 대한 접근 권한이 없습니다.");
        }

        // 2. 해당 유저의 보관함에 아이템이 있는지 확인
        UserItemStorageEntity existing = userItemStorageRepo
                .findByUserIdAndItemIdAndForestId(userId, itemId, forestId)
                .orElse(null);

        if (existing != null) {
            // 보유 중이면 count + 1
            existing.setTotalCount(existing.getTotalCount() + 1);
            existing.setPlacedCount(existing.getPlacedCount() + 1);
            userItemStorageRepo.save(existing);
        } else {
            // 보유하지 않은 경우 새로 저장
            UserItemStorageEntity newItem = UserItemStorageEntity.builder()
                    .userId(userId)
                    .itemId(itemId)
                    .forestId(forestId)
                    .totalCount(1)
                    .placedCount(0)
                    .build();
            userItemStorageRepo.save(newItem);
        }
    }

}
