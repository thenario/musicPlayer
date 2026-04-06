package com.kyf.mp.javaserver.service;

import com.kyf.mp.javaserver.entity.Users;
import com.kyf.mp.javaserver.modelVO.LoginVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kyf.mp.javaserver.common.*;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface IUsersService extends IService<Users> {
    ResultModel<LoginVO> login(String username, String password);

    ResultModel<String> register(Users user);
}
