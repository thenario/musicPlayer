package com.kyf.mp.javaserver.modules.usermodule.business.businessImp;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.usermodule.business.IUsersBusiness;
import com.kyf.mp.javaserver.modules.usermodule.dto.EditUserDTO;
import com.kyf.mp.javaserver.modules.usermodule.entity.Users;
import com.kyf.mp.javaserver.modules.usermodule.mapper.UsersMapper;
import com.kyf.mp.javaserver.modules.usermodule.vo.EditVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户数据访问实现：复杂数据库操作。
 */
@Service
@Slf4j
public class UsersBusinessImpl extends BaseBusinessImpl<UsersMapper, Users> implements IUsersBusiness {

    @Value("${file.static.user-cover-url}")
    private String userCoverUrl;

    @Value("${file.upload.user-cover-path}")
    private String userCoverPath;

    @Override
    public EditVO editUserProfile(EditUserDTO editData, Long userId) {
        Users oldUser = baseMapper.selectById(userId);
        if (oldUser == null)
            throw new BusinessException(404, "用户不存在");

        Users newUser = new Users();
        newUser.setUserId(userId);
        boolean isChanged = false;

        if (editData.getUser_name() != null && !editData.getUser_name().trim().isEmpty()) {
            newUser.setUserName(editData.getUser_name());
            isChanged = true;
        }

        MultipartFile file = editData.getUser_cover();
        if (file != null && !file.isEmpty()) {
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String newFileName = userId + "_" + System.currentTimeMillis() + suffix;

            File directory = new File(userCoverPath);
            if (directory.exists()) {
                File[] oldFiles = directory.listFiles((dir, name) -> name.startsWith(userId + "_"));
                if (oldFiles != null) {
                    for (File oldFile : oldFiles)
                        oldFile.delete();
                }
            } else {
                directory.mkdirs();
            }

            try {
                File dest = new File(userCoverPath + newFileName);
                file.transferTo(dest);

                String fullUrl = userCoverUrl + newFileName;
                newUser.setUserCoverUrl(fullUrl);
                isChanged = true;
            } catch (IOException e) {
                log.error("用户ID: {} 封面写入硬盘失败", userId, e);
                throw new BusinessException(500, "文件保存失败");
            }
        }

        if (isChanged) {
            baseMapper.updateById(newUser);
        }

        EditVO vo = new EditVO();
        vo.setUserName(newUser.getUserName() != null ? newUser.getUserName() : oldUser.getUserName());
        vo.setUserCoverUrl(newUser.getUserCoverUrl() != null ? newUser.getUserCoverUrl() : oldUser.getUserCoverUrl());

        return vo;
    }
}
