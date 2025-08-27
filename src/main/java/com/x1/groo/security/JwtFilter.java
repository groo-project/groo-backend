package com.x1.groo.security;

import com.x1.groo.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;



@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
//    private final TokenProvider tokenProvider;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    private static final List<String> EXCLUDE_URLS = List.of(
            "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return EXCLUDE_URLS.stream().anyMatch(path::startsWith);
    }

    private static final java.util.Set<String> SKIP = java.util.Set.of(
            "/api/auth/login", "/api/auth/refresh", "/api/auth/signup", "/api/auth/logout"
    );


    // 실제 필터링 로직은 doFilterInternal 에 들어감
    // JWT 토큰의 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할 수행
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {


        // Request Header 에서 토큰을 꺼냄
//        String accessToken = resolveAccessToken(request);

        String uri = request.getRequestURI();


        // ✅ 예외 경로/OPTIONS는 무조건 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || SKIP.contains(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 없으면 건드리지 말고 통과
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = auth.substring(7);

        if(jwtUtil.validationAccessToken(accessToken)) {

            // 유효한 토큰을 통해 아이디와 권한들을 가진 Authentication 추출 (Spring Security가 인식할 수 있게 반환)
            Authentication authentication = jwtUtil.getAuthentication(accessToken);

            // Spring Security가 인식할 수 있게 주입(요청당 저장 할 수 있는 공간인 LocalThread에 저장)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);

    }

    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("AUTHORIZATION");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
