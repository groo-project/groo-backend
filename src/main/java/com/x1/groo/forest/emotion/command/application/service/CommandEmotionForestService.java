package com.x1.groo.forest.emotion.command.application.service;

import com.x1.groo.forest.emotion.command.domain.vo.*;

import java.util.List;

public interface CommandEmotionForestService {
    void retrieveItemsByIds(int userId, List<Integer> placementIds);

    void retrieveAllItems(int userId, int forestId);

    void placeItem(int userId, RequestPlacementVO requestPlacementVO);

    void replaceItem(int userId, List<RequestReplacementVO> replacementVOList);

    void createMailbox(int userId, RequestMailboxVO requestMailboxVO);

    void deleteMailbox(int userId, int mailboxId, int forestId);

    void updateForestPublic(int forestId, int userId);

    void createEmotionForest(int userId, RequestCreateVO request);

    void updateForestName(int forestId, int userId, String newName);

    void placeStoredItem(int userId, RequestReplantVO requestReplantVO);

    void saveForestItem(int userId, RequestSaveForestItemVO vo);
}
