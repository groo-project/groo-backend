package com.x1.groo.security;

import com.x1.groo.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    @Autowired
    public JwtAuthenticationProvider(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // authentication : filter 에서 넘어온 token
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 넘어온 토큰에서 사용자가 로그인 시 email, pwd 추출
        String email = authentication.getName();
        String pwd = authentication.getCredentials().toString();

        // 해당 email과 일치한 db에서 조회된 회원
        CustomUserDetails userDetails = (CustomUserDetails) userService.loadUserByUsername(email);

        if (!passwordEncoder.matches(pwd, userDetails.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
