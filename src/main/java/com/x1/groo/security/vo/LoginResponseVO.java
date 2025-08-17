package com.x1.groo.security.vo;

import com.x1.groo.user.aggregate.UserEntity;
import lombok.*;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LoginResponseVO {
    private int id;
    private String email;
    private String nickName;

    public static LoginResponseVO of(UserEntity userEntity) {
        return new LoginResponseVO();
    }

}
