package com.x1.groo.auth.command.application.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.x1.groo.auth.command.application.vo.GoogleLoginRequestVO;
import com.x1.groo.auth.command.application.vo.RefreshResult;
import com.x1.groo.user.dto.LoginDTO;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AuthCommandService {
    RefreshResult refresh(String rt);

    LoginDTO loginOrRegisterGoogleUser(String idTokenString) throws GeneralSecurityException, IOException;
}
