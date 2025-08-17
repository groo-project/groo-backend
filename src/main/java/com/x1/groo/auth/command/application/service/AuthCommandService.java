package com.x1.groo.auth.command.application.service;

import com.x1.groo.auth.command.application.vo.RefreshResult;

public interface AuthCommandService {
    RefreshResult refresh(String rt);
}
