package com.x1.groo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.user.vo.SignupRequestVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private Environment env;
    private JwtUtil jwtUtil;

    public AuthenticationFilter(AuthenticationManager authenticationManager, Environment env, JwtUtil jwtUtil) {
        super(authenticationManager);
        this.env = env;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/auth/login");

    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        try {
            SignupRequestVO creds = new ObjectMapper().readValue(request.getInputStream(), SignupRequestVO.class);

            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword());

            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
//        SecurityContext에 auth를 넣어주는 기본 로직
//        super.successfulAuthentication(request, response, chain, authResult);


        // 토큰의 payload에 id, 가진 권한들, 만료시간)
        String email = authResult.getName();

        List<String> roles = authResult.getAuthorities().stream()
//                .map(role -> role.getAuthority())
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(userDetails);

        var body = new java.util.HashMap<String, Object>();
        body.put("accessToken", accessToken);
        // 필요하면 닉네임도 추가
        // body.put("userNickname", userNickname);

        response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body));
        response.getWriter().flush();



        Claims claims = Jwts.claims().setSubject(email); // -> 등록된 클레임 지님
        claims.put("auth", roles); // auth란 이름으로 roles라는 권한을 기록  -> 비공개클레임으로 만듦

        String token = Jwts.builder()
                .setClaims(claims)  // 등록된 클레임(subject) + 비공개 클레임(auth)
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(env.getProperty("token.expiration_time"))))
                .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
                .compact();
    }
}
