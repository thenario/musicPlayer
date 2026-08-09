package com.kyf.mp.javaserver.modules.UserModule.VO;

import lombok.Data;

@Data
public class LoginVO {
    private UserVO user;
    private String token;
}
