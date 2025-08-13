package com.x1.groo.user.dto;

import com.x1.groo.security.vo.LoginResponseVO;
import com.x1.groo.user.aggregate.Role;
import com.x1.groo.user.aggregate.UserEntity;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserDTO {
    private int id;
    private String email;
    private String password;
    private String nickname;
    private String role;

//    /** DTO -> Entity */
//    public UserEntity toEntity() {
//        return UserEntity.builder()
//                // id가 0이면 새 엔티티로 간주(=null)
//                .id(this.id == 0 ? null : this.id)
//                .email(this.email)
//                .password(this.password)     // ⚠️ 저장 전에는 서비스에서 반드시 encode 하세요!
//                .nickname(this.nickname)
////                .role(this.role)             // ▼ 아래 주석 참고해서 필요 시 변경
//                 .role(Role.valueOf(this.role))        // Role이 enum이면
//                // .role(roleRepository.findByCode(this.role).orElseThrow(...)) // Role이 엔티티면
//                // .roles(Set.of(new Role(this.role)))   // roles 컬렉션이면
//                .build();
//    }
    public static UserDTO fromEntity(UserEntity e) {
        return UserDTO.builder()
                .id(e.getId())
                .email(e.getEmail())
                .password(e.getPassword())   // 해시 그대로
                .nickname(e.getNickname())
//                .role(e.getRole())           // 엔티티가 단일 role 문자열일 때
                .build();
}


}

