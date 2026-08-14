package com.kyf.mp.server.modules.user.vo;

import lombok.Data;

@Data
public class LoginVO {
    private UserVO user;
    private String token;
}
