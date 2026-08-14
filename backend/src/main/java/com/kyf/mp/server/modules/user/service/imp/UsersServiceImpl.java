package com.kyf.mp.server.modules.user.service.imp;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.modules.user.business.UsersBusiness;
import com.kyf.mp.server.modules.user.dto.EditUserDTO;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.service.UsersService;
import com.kyf.mp.server.modules.user.vo.EditVO;
import com.kyf.mp.server.modules.user.vo.LoginVO;
import com.kyf.mp.server.modules.user.vo.UserVO;
import com.kyf.mp.server.utils.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类：业务逻辑编排（登录/注册），数据访问委托给 UsersBusiness。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsersServiceImpl implements UsersService {

    private final UsersBusiness usersBusiness;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(String username, String password) {
        log.info("===> 收到登录请求: username={}", username);
        // 简单查询：直接用 business 的基础 CRUD
        Users user = usersBusiness.lambdaQuery().eq(Users::getUserName, username).one();

        if (user == null) {
            throw new BusinessException(404, "账号不存在");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "密码错误");
        }

        UserVO cleanUser = new UserVO();
        BeanUtils.copyProperties(user, cleanUser);

        String token = JwtUtils.createToken(user.getUserId(), user.getUserName());

        LoginVO vo = new LoginVO();
        vo.setUser(cleanUser);
        vo.setToken(token);

        log.info("===> 业务逻辑执行完毕，准备返回: {}", vo);
        return vo;
    }

    @Override
    public void register(Users user) {
        // 唯一性校验：直接用 business 的基础 CRUD
        long count = usersBusiness.lambdaQuery()
                .eq(Users::getUserName, user.getUserName())
                .or()
                .eq(Users::getUserEmail, user.getUserEmail())
                .count();

        if (count > 0) {
            throw new BusinessException(409, "该用户名或邮箱已被注册");
        }

        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);

        boolean saved = usersBusiness.save(user);
        if (!saved) {
            throw new BusinessException(500, "注册失败，数据库写入异常");
        }
    }

    @Override
    public Map<String, String> getUserCoverUrl(Long userId) {
        Users user = usersBusiness.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        String userCover = user.getUserCoverUrl();

        if (userCover == null || userCover.trim().isEmpty()) {
            throw new BusinessException(404, "该用户尚未上传封面");
        }

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("userCoverUrl", userCover);

        return resultMap;
    }

    @Override
    public EditVO editUserProfile(EditUserDTO editData, Long userId) {
        return usersBusiness.editUserProfile(editData, userId);
    }
}
