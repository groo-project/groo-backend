package com.x1.groo.security.config;


import com.x1.groo.security.AuthenticationFilter;
import com.x1.groo.security.JwtAuthenticationProvider;
import com.x1.groo.security.JwtFilter;
import com.x1.groo.security.jwt.JwtAccessDeniedHandler;
import com.x1.groo.security.jwt.JwtAuthenticationEntryPoint;
import com.x1.groo.security.util.JwtUtil;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig  {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final JwtUtil jwtUtil;
    private final Environment env;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;


    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(jwtAuthenticationProvider));
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth

                        // ADMIN만 가능
                        .requestMatchers("/api/logs/**").hasRole("ADMIN")

                        // health 체크
                        .requestMatchers("/health/**").permitAll()

                        // swagger
                        .requestMatchers("/swagger",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs/**")
                        .permitAll()

                        // 모두 접근 갸능
                        .requestMatchers("/auth/**",
                                "/mails/**",
                                "/image/**")
                        .permitAll()

                        // 로그인 필요
                        .anyRequest().authenticated()
                )

                .authenticationManager(authenticationManager())
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(e -> e
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)      // 401 JSON
                .accessDeniedHandler(jwtAccessDeniedHandler)        // 403 JSON
        );

        http.addFilter(getauthenticationFilter(authenticationManager()));

        http.addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    private Filter getauthenticationFilter(AuthenticationManager authenticationManager) {
        return new AuthenticationFilter(authenticationManager, env, jwtUtil);
    }


}
