package com.kyf.mp.javaserver.modules.UserModule.serviceImp;

import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.UserModule.VO.LoginVO;
import com.kyf.mp.javaserver.modules.UserModule.VO.UserVO;
import com.kyf.mp.javaserver.modules.UserModule.entity.Users;
import com.kyf.mp.javaserver.modules.UserModule.mapper.UsersMapper;
import com.kyf.mp.javaserver.modules.UserModule.service.IUsersService;
import com.kyf.mp.javaserver.utils.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public ResultModel<LoginVO> login(String username, String password) {
        log.info("===> 收到登录请求: username={}", username);
        Users user = this.getOne(new LambdaQueryWrapper<Users>().eq(Users::getUserName, username));

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

        log.info("===> 业务逻辑执行完毕，准备返回 ResultModel: {}", vo);
        ResultModel<LoginVO> result = ResultModel.success(vo);

        // 增加一步：手动检查 result 对象
        log.info("===> 封装后的数据: code={}, message={}", result.getCode(), result.getMessage());
        return ResultModel.success(vo);
    }

    @Override
    public ResultModel<String> register(Users user) {
        long count = this.count(new LambdaQueryWrapper<Users>()
                .eq(Users::getUserName, user.getUserName())
                .or()
                .eq(Users::getUserEmail, user.getUserEmail()));

        if (count > 0) {
            throw new BusinessException(409, "该用户名或邮箱已被注册");
        }

        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);

        boolean saved = this.save(user);
        if (!saved) {
            throw new BusinessException(500, "注册失败，数据库写入异常");
        }

        return ResultModel.success(null);
    }
}