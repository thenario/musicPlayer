package com.kyf.mp.javaserver.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.entity.Users;
import com.kyf.mp.javaserver.modelVO.LoginVO;
import com.kyf.mp.javaserver.service.IUsersService;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final IUsersService userService;

    @PostMapping("/login")
    public ResultModel<LoginVO> login(@RequestBody Users user) {
        return userService.login(user.getUserName(), user.getPassword());
    }

    @PostMapping("/register")
    public ResultModel<String> register(@RequestBody Users user) {
        return userService.register(user);
    }

    @PostMapping("/logout")
    public ResultModel<Object> logout() {
        ResultModel<Object> result = ResultModel.success(null);
        result.setMessage("登出成功，期待下次再见");
        return result;
    }
}
