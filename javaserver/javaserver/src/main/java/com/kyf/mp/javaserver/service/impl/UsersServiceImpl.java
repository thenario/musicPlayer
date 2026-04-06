package com.kyf.mp.javaserver.service.impl;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.entity.Users;
import com.kyf.mp.javaserver.mapper.UsersMapper;
import com.kyf.mp.javaserver.modelVO.LoginVO;
import com.kyf.mp.javaserver.modelVO.UserVO;
import com.kyf.mp.javaserver.service.IUsersService;
import com.kyf.mp.javaserver.utils.JwtUtils;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.BeanUtils;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@RequiredArgsConstructor
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public ResultModel<LoginVO> login(String username, String password) {
        Users user = this.getOne(new LambdaQueryWrapper<Users>().eq(Users::getUserName, username));
        if (user == null) {
            return ResultModel.error("账号不存在", 404);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResultModel.error("密码错误", 401);
        }
        UserVO cleanUser = new UserVO();
        BeanUtils.copyProperties(user, cleanUser);
        String token = JwtUtils.createToken(user.getUserId(), user.getUserName());
        LoginVO vo = new LoginVO();
        vo.setUser(cleanUser);
        vo.setToken(token);
        return ResultModel.success(vo);
    }

    @Override
    public ResultModel<String> register(Users user) {
        long count = this.count(new LambdaQueryWrapper<Users>()
                .eq(Users::getUserName, user.getUserName())
                .or()
                .eq(Users::getUserEmail, user.getUserEmail()));

        if (count > 0) {
            return ResultModel.error("该用户名或邮箱已被注册", 409);
        }
        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);
        this.save(user);
        return ResultModel.success("注册成功");

    }
}
