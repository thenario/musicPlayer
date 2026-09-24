package com.kyf.mp.server.modules.user.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.common.auth.LoginRateLimiter;
import com.kyf.mp.server.modules.user.dto.EditUserDTO;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.repository.UsersRepository;
import com.kyf.mp.server.modules.user.service.UsersService;
import com.kyf.mp.server.modules.user.service.workflow.UsersWorkflow;
import com.kyf.mp.server.modules.user.vo.EditVO;
import com.kyf.mp.server.modules.user.vo.LoginVO;
import com.kyf.mp.server.modules.user.vo.UserVO;
import com.kyf.mp.server.utils.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类：业务逻辑编排（登录/注册），业务流程委托给 UsersWorkflow。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsersServiceImpl implements UsersService {

    private final UsersWorkflow usersWorkflow;
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final LoginRateLimiter loginRateLimiter;

    @Override
    public LoginVO login(String username, String password) {
        log.info("===> 收到登录请求: username={}", username);
        loginRateLimiter.check(username);
        // Repository 查询数据，Service 判断登录规则。
        Users user = usersRepository.findByName(username);

        if (user == null || !matchesPassword(password, user)) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        UserVO cleanUser = new UserVO();
        BeanUtils.copyProperties(user, cleanUser);

        String token = jwtUtils.createToken(user.getUserId(), user.getUserName());

        LoginVO vo = new LoginVO();
        vo.setUser(cleanUser);
        vo.setToken(token);

        log.info("登录成功: userId={}, username={}", user.getUserId(), user.getUserName());
        return vo;
    }

    boolean matchesPassword(String password, Users user) {
        String storedPassword = user.getPassword();
        if (storedPassword.matches("^[a-fA-F0-9]{64}$")) {
            boolean matches = MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8),
                    storedPassword.getBytes(StandardCharsets.UTF_8));
            if (matches) {
                user.setPassword(passwordEncoder.encode(password));
                usersRepository.updateById(user);
            }
            return matches;
        }
        return passwordEncoder.matches(password, storedPassword);
    }

    @Override
    public void register(Users user) {
        // Repository 提供查询结果，Service 判断是否允许注册。
        long count = usersRepository.countByNameOrEmail(user.getUserName(), user.getUserEmail());

        if (count > 0) {
            throw new BusinessException(409, "该用户名或邮箱已被注册");
        }

        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);

        try {
            boolean saved = usersRepository.save(user);
            if (!saved) {
                throw new BusinessException(500, "注册失败，数据库写入异常");
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException(409, "该用户名或邮箱已被注册");
        }
    }

    @Override
    public Map<String, String> getUserCoverUrl(Long userId) {
        Users user = usersRepository.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        String userCover = user.getUserCoverUrl();

        if (userCover == null || userCover.trim().isEmpty()) {
            throw new BusinessException(404, "该用户尚未上传封面");
        }

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("user_cover_url", userCover);

        return resultMap;
    }

    @Override
    public EditVO editUserProfile(EditUserDTO editData, Long userId) {
        return usersWorkflow.editUserProfile(editData, userId);
    }
}
