package com.x1.groo.security;

import com.x1.groo.user.dto.UserDTO;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final UserDTO user;

    public CustomUserDetails(UserDTO user) {
        this.user = user;
    }

    // 🔧 추가: DTO -> UserDetails 변환 헬퍼
//    public static CustomUserDetails from(UserDTO dto) {
//        return CustomUserDetails.builder()
//                .user(dto)   // ✅ 빌더에는 user(...)만 존재
//                .build();
//    }

    // DTO -> UserDetails 헬퍼
    public static CustomUserDetails from(UserDTO dto) {
        return new CustomUserDetails(dto); // ✅ 빌더 사용 안 함
    }


    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }


    public int getUserId() {
        return user.getId();
    }

    public String getName() {
        return user.getNickname();
    }

    public int getForestId() {
        return user.getForestId();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

}
