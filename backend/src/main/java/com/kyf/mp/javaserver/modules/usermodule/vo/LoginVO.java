package com.kyf.mp.javaserver.modules.usermodule.vo;

import lombok.Data;

@Data
public class LoginVO {
    private UserVO user;
    private String token;
}
