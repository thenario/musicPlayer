package com.kyf.mp.javaserver.modules.usermodule.service;

import java.util.Map;

import com.kyf.mp.javaserver.modules.usermodule.dto.EditUserDTO;
import com.kyf.mp.javaserver.modules.usermodule.entity.Users;
import com.kyf.mp.javaserver.modules.usermodule.vo.EditVO;
import com.kyf.mp.javaserver.modules.usermodule.vo.LoginVO;

/**
 * <p>
 * 服务类：业务逻辑层，不再继承 IService（基础 CRUD 由 business 层提供）。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface IUsersService {
    LoginVO login(String username, String password);

    void register(Users user);

    EditVO editUserProfile(EditUserDTO editData, Long userId);

    Map<String, String> getUserCoverUrl(Long userId);
}
