package com.x1.groo.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        // ★ 프론트에서 Authorization 헤더를 읽을 수 있게 노출
        config.addExposedHeader("Authorization");

        // todo : 쿠키 기반을 쓰면 CORS 설정에서 credentials: true 허용 필요.
        //  예: Spring Security → config.setAllowCredentials(true)
        config.setAllowCredentials(true);

        // 쿠키 발급 예시 (Set-Cookie)
//        ResponseCookie cookie = ResponseCookie.from("refreshToken", rt)
//                .httpOnly(true)
//                .secure(true)             // HTTPS에서 true
//                .sameSite("Lax")          // 또는 Strict
//                .path("/api/auth")        // 필요한 범위로
//                .maxAge(Duration.ofDays(14))
//                .build();
//        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
