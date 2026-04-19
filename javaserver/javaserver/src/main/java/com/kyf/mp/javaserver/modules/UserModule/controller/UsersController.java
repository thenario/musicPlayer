package com.kyf.mp.javaserver.modules.UserModule.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.UserModule.DTO.EditUserDTO;
import com.kyf.mp.javaserver.modules.UserModule.VO.EditVO;
import com.kyf.mp.javaserver.modules.UserModule.VO.LoginVO;
import com.kyf.mp.javaserver.modules.UserModule.entity.Users;
import com.kyf.mp.javaserver.modules.UserModule.service.IUsersService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @PatchMapping("/me")
    public ResultModel<EditVO> editUserProfile(EditUserDTO editData, @RequestAttribute("userId") Integer userId) {
        return userService.editUserProfile(editData, userId);
    }

    @GetMapping("path")
    public ResultModel<Map<String, String>> getUserCoverUrl(@RequestAttribute("userId") Integer userId) {
        return userService.getUserCoverUrl(userId);
    }

}
