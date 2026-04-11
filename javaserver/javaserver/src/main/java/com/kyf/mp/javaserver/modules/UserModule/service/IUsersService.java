package com.kyf.mp.javaserver.modules.UserModule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyf.mp.javaserver.common.*;
import com.kyf.mp.javaserver.modules.UserModule.VO.LoginVO;
import com.kyf.mp.javaserver.modules.UserModule.entity.Users;

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
