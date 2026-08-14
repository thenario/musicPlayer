package com.kyf.mp.server.modules.user.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kyf.mp.server.common.ResultModel;
import com.kyf.mp.server.common.auth.JwtAuthenticationFilter;
import com.kyf.mp.server.common.auth.TokenBlacklistService;
import com.kyf.mp.server.modules.user.dto.EditUserDTO;
import com.kyf.mp.server.modules.user.vo.EditVO;
import com.kyf.mp.server.modules.user.vo.LoginVO;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.service.UsersService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService userService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResultModel<LoginVO> login(@RequestBody @Valid Users user) {
        return ResultModel.success(userService.login(user.getUserName(), user.getPassword()));
    }

    @PostMapping("/register")
    public ResultModel<String> register(@RequestBody @Valid Users user) {
        userService.register(user);
        return ResultModel.success(null);
    }

    @PostMapping("/logout")
    public ResultModel<Object> logout(
            @RequestAttribute(value = JwtAuthenticationFilter.TOKEN_ATTRIBUTE, required = false) String token) {
        tokenBlacklistService.revoke(token);
        ResultModel<Object> result = ResultModel.success(null);
        result.setMessage("登出成功，期待下次再见");
        return result;
    }

    @PatchMapping("/me")
    public ResultModel<EditVO> editUserProfile(EditUserDTO editData, @RequestAttribute("userId") Long userId) {
        return ResultModel.success(userService.editUserProfile(editData, userId));
    }

    @GetMapping("/users/cover")
    public ResultModel<Map<String, String>> getUserCoverUrl(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") Long userId) {
        return ResultModel.success(userService.getUserCoverUrl(userId));
    }

}
