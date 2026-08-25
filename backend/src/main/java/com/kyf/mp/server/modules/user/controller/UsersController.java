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
import com.kyf.mp.server.modules.user.dto.LoginRequestDTO;
import com.kyf.mp.server.modules.user.dto.RegisterRequestDTO;
import com.kyf.mp.server.modules.user.vo.EditVO;
import com.kyf.mp.server.modules.user.vo.LoginVO;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.service.UsersService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "用户与认证", description = "注册、登录、退出和个人资料管理")
public class UsersController {

    private final UsersService userService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，成功后返回 JWT")
    public ResultModel<LoginVO> login(@RequestBody @Valid LoginRequestDTO request) {
        return ResultModel.success(userService.login(request.getUserName(), request.getPassword()));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用用户名、邮箱和密码注册新用户")
    public ResultModel<Void> register(@RequestBody @Valid RegisterRequestDTO request) {
        Users user = new Users();
        user.setUserName(request.getUserName());
        user.setUserEmail(request.getUserEmail());
        user.setPassword(request.getPassword());
        userService.register(user);
        return ResultModel.success(null);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出登录", description = "将当前 JWT 加入 Redis 黑名单，使其立即失效")
    @SecurityRequirement(name = "bearerAuth")
    public ResultModel<Object> logout(
            @Parameter(hidden = true)
            @RequestAttribute(value = JwtAuthenticationFilter.TOKEN_ATTRIBUTE, required = false) String token) {
        tokenBlacklistService.revoke(token);
        ResultModel<Object> result = ResultModel.success(null);
        result.setMessage("登出成功，期待下次再见");
        return result;
    }

    @PatchMapping("/me")
    @Operation(summary = "编辑个人资料", description = "修改当前用户的昵称或头像")
    @SecurityRequirement(name = "bearerAuth")
    public ResultModel<EditVO> editUserProfile(EditUserDTO editData,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return ResultModel.success(userService.editUserProfile(editData, userId));
    }

    @GetMapping("/cover")
    @Operation(summary = "获取当前用户头像地址")
    @SecurityRequirement(name = "bearerAuth")
    public ResultModel<Map<String, String>> getUserCoverUrl(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") Long userId) {
        return ResultModel.success(userService.getUserCoverUrl(userId));
    }

    @GetMapping("/auth")
    @Operation(summary = "验证当前登录状态", description = "JWT 校验通过时返回成功")
    @SecurityRequirement(name = "bearerAuth")
    public ResultModel<Void> authentication() {
        return ResultModel.success(null);
    }

}
