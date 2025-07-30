package com.x1.groo.forest.emotion.command.application.service;

import com.x1.groo.forest.emotion.command.domain.vo.RequestCreateVO;
import com.x1.groo.forest.emotion.command.domain.vo.RequestMailboxVO;
import com.x1.groo.forest.emotion.command.domain.vo.RequestPlacementVO;
import com.x1.groo.forest.emotion.command.domain.vo.RequestReplacementVO;

public interface CommandEmotionForestService {
    void retrieveItemById(int userId, int placementId);

    void retrieveAllItems(int userId, int forestId);

    void placeItem(int userId, RequestPlacementVO requestPlacementVO);

    void replaceItem(int userId, RequestReplacementVO requestReplacementVO);

    void createMailbox(int userId, RequestMailboxVO requestMailboxVO);

    void deleteMailbox(int userId, int mailboxId, int forestId);

    void updateForestPublic(int forestId, int userId);

    void createEmotionForest(int userId, RequestCreateVO request);

    void updateForestName(int forestId, int userId, String newName);
}
