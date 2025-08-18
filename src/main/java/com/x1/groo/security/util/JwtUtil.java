package com.x1.groo.security.util;

import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.user.dto.UserDTO;
import com.x1.groo.user.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtil {

    private final Key key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;
    private final UserService userService;

    @Autowired
    public JwtUtil(@Value("${token.secret}") String secretBase64,
                   @Value("${token.access_expiration_time}") long accessExpirationMs,
                   UserService userService,
                   @Value("${token.refresh_expiration_time}") long refreshExpirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.userService = userService;
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Date now = new Date();

        // 권한 문자열 목록
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toList());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("userId", userDetails.getUserId())
                .claim("auth", roles)
                .claim("tokenType", "access")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshExpirationMs))
                .claim("tokenType", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // token 검증 (우리가 사이트의 secret key로 만들어 졌는지, 내용이 비어져 있지 않은지)
    public boolean validationAccessToken(String accessToken) {

        if (!StringUtils.hasText(accessToken)) { // Authorization 헤더에 토큰이 없거나 공백
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("유효하지 않은 JWT Token");
        } catch (ExpiredJwtException e) {
            log.info("만료기간이 지남");
        } catch (UnsupportedJwtException e) {
            log.info("지원하지 않는 JWT Token");
        } catch (IllegalArgumentException e) {
            log.info("토큰 문자열이 null/빈 값");
        }
        return false;
    }

    // 유효성 검증이 된 토큰에서 인증 객체 반환
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parserClaims(accessToken);

        // 토큰에 들어있는 이메일로 DB에서 회원 조회하고 UserDetails로 가져옴
        UserDetails userDetails = userService.loadUserByUsername(claims.getSubject());

        // 토큰에 들어있는 권한들을 List<GrantedAuthority>
        Collection<GrantedAuthority> authorities = null;
        if (claims.get("auth") == null) { // 권한이 없으면
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        } else {  // 권한이 있으면
            authorities =
                    Arrays.stream(claims.get("auth").toString()
                                    .replace("[", "")
                                    .replace("]", "")
                                    .split(", "))
                            .map(role -> new SimpleGrantedAuthority(role))
                            .collect(Collectors.toList());
        }

        int userId = claims.get("userId", Number.class).intValue();
        String email = claims.getSubject(); // setSubject(getUsername()) 했었죠
        String nickname = claims.get("nickname", String.class);


        UserDTO dto = UserDTO.builder()
                .id(userId)
                .email(email)
                .nickname(nickname)
                .build();

        CustomUserDetails principal = new CustomUserDetails(dto, authorities);
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);

    }

    // 토큰에서 payload에 담긴 클레임들만 추출
    private Claims parserClaims(String accessToken) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
    }


}