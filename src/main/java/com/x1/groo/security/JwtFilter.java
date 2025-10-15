package com.x1.groo.security;

import com.x1.groo.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final AntPathMatcher PM = new AntPathMatcher();

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {


        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // CORS preflight
        String path = request.getRequestURI();
        for (String p : SKIP_PATTERNS) {
            if (PM.match(p, path)) return true; //  공개 경로는 아예 필터 스킵
        }
        return false;
    }

    private static final java.util.Set<String> SKIP_PATTERNS = java.util.Set.of(
            "/health/**", "/healthz", "/actuator/**",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/api/auth/login", "/api/auth/register", "/api/auth/reissue",
             "/api/mails/**", "/api/image/**"
    );


    // 실제 필터링 로직은 doFilterInternal 에 들어감
    // JWT 토큰의 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할 수행
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {



        //  이미 인증돼 있으면 통과
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // authorization -> accessToken
        String token = resolveAccessToken(request);

        log.info("JWT Header: {}", request.getHeader("Authorization"));
        log.info("JWT is valid: {}", jwtUtil.validationAccessToken(token));

        if (token == null) {
            token = getCookieValue(request, "accessToken");
        }

        //  토큰이 없거나 빈 값이면 ‘익명’으로 통과 (보호 URL은 Security가 막음)
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }


        if(jwtUtil.validationAccessToken(token)) {

            // 유효한 토큰을 통해 아이디와 권한들을 가진 Authentication 추출 (Spring Security가 인식할 수 있게 반환)
            Authentication authentication = jwtUtil.getAuthentication(token);

            // Spring Security가 인식할 수 있게 주입(요청당 저장 할 수 있는 공간인 LocalThread에 저장)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);

    }

    private String getCookieValue(HttpServletRequest req, String accessToken) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (accessToken.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
