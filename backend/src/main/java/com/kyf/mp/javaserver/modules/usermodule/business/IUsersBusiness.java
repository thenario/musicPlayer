package com.kyf.mp.javaserver.modules.usermodule.business;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.common.business.IBaseBusiness;
import com.kyf.mp.javaserver.modules.usermodule.dto.EditUserDTO;
import com.kyf.mp.javaserver.modules.usermodule.entity.Users;
import com.kyf.mp.javaserver.modules.usermodule.vo.EditVO;

/**
 * 用户数据访问层：复杂数据库操作在此定义。
 */
public interface IUsersBusiness extends IBaseBusiness<Users> {
    ResultModel<EditVO> editUserProfile(EditUserDTO editData, Integer userId);
}
