package com.kyf.mp.server.modules.user.business;

import com.kyf.mp.server.common.business.BaseBusiness;
import com.kyf.mp.server.modules.user.dto.EditUserDTO;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.vo.EditVO;

/**
 * 用户数据访问层：复杂数据库操作在此定义。
 */
public interface UsersBusiness extends BaseBusiness<Users> {
    EditVO editUserProfile(EditUserDTO editData, Long userId);
}
