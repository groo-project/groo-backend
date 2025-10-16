package com.x1.groo.auth.command.application.service;

import com.x1.groo.auth.command.application.vo.RefreshResultVO;
import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.user.dto.LoginDTO;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AuthCommandService {
    RefreshResultVO refresh(String rt);

    LoginDTO loginOrRegisterGoogleUser(String idTokenString) throws GeneralSecurityException, IOException;

    void withdraw(CustomUserDetails user);
}
