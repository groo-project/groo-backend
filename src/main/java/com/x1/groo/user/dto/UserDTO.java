package com.x1.groo.user.dto;

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

//    public static UserDTO of(UserEntity loginUser) {
//        if (loginUser == null) {
//            throw new IllegalArgumentException("UserEntity is null");
//        }
//        return UserDTO.builder()
//                .id(loginUser.getId())                 // int/Long 맞춰서
//                .email(loginUser.getEmail())
//                .password(loginUser.getPassword())     // 외부 응답에 쓰지 않을 거면 @JsonIgnore 권장
//                .nickname(loginUser.getNickname())
////                .type(loginUser.getType())             // enum ROLE/TYPE 같은 거 쓰면 그대로
//                // .status(e.getStatus())      // 있으면 추가
//                .build();
//    }
}
