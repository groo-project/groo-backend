package com.x1.groo.forest.mate.command.application.service;

import com.x1.groo.forest.mate.command.domain.vo.CreateMateForestRequest;

public interface CommandMateService {
    String createInviteLink(int forestId);

    void acceptInvite(int userId, String inviteCode);

    void quit(int userId, int forestId);

    void createMateForest(int userId, CreateMateForestRequest request);
}
