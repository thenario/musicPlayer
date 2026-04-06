package com.kyf.mp.javaserver.modelVO;

import lombok.Data;

@Data
public class LoginVO {
    private UserVO user;
    private String token;
}
