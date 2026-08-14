package com.kyf.mp.server.modules.user.business.imp;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.common.file.UploadFileValidator;
import com.kyf.mp.server.modules.user.business.UsersBusiness;
import com.kyf.mp.server.modules.user.dto.EditUserDTO;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.mapper.UsersMapper;
import com.kyf.mp.server.modules.user.vo.EditVO;

import lombok.extern.slf4j.Slf4j;

/** 用户数据访问实现：复杂数据库操作。 */
@Service
@Slf4j
public class UsersBusinessImpl extends BaseBusinessImpl<UsersMapper, Users> implements UsersBusiness {

    @Value("${file.static.user-cover-url}")
    private String userCoverUrl;

    @Value("${file.upload.user-cover-path}")
    private String userCoverPath;

    @Override
    public EditVO editUserProfile(EditUserDTO editData, Long userId) {
        Users oldUser = baseMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        Users newUser = new Users();
        newUser.setUserId(userId);
        boolean isChanged = false;
        File savedCoverFile = null;

        if (editData.getUser_name() != null && !editData.getUser_name().trim().isEmpty()) {
            newUser.setUserName(editData.getUser_name());
            isChanged = true;
        }

        MultipartFile file = editData.getUser_cover();
        if (file != null && !file.isEmpty()) {
            String extension = UploadFileValidator.validateImage(file);
            String newFileName = userId + "_" + System.currentTimeMillis() + "." + extension;
            File directory = new File(userCoverPath);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new BusinessException(500, "头像目录创建失败");
            }

            try {
                savedCoverFile = new File(directory, newFileName);
                file.transferTo(savedCoverFile);
                newUser.setUserCoverUrl(userCoverUrl + newFileName);
                isChanged = true;
            } catch (IOException e) {
                deleteFile(savedCoverFile);
                log.error("用户ID: {} 封面写入硬盘失败", userId, e);
                throw new BusinessException(500, "文件保存失败");
            }
        }

        if (isChanged) {
            try {
                baseMapper.updateById(newUser);
            } catch (RuntimeException e) {
                deleteFile(savedCoverFile);
                throw e;
            }
        }
        if (savedCoverFile != null) {
            cleanupPreviousCovers(savedCoverFile.getParentFile(), userId, savedCoverFile.getName());
        }

        EditVO vo = new EditVO();
        vo.setUserName(newUser.getUserName() != null ? newUser.getUserName() : oldUser.getUserName());
        vo.setUserCoverUrl(newUser.getUserCoverUrl() != null ? newUser.getUserCoverUrl() : oldUser.getUserCoverUrl());
        return vo;
    }

    private void cleanupPreviousCovers(File directory, Long userId, String currentFileName) {
        File[] oldFiles = directory.listFiles(
                (dir, name) -> name.startsWith(userId + "_") && !name.equals(currentFileName));
        if (oldFiles == null) {
            return;
        }
        for (File oldFile : oldFiles) {
            deleteFile(oldFile);
        }
    }

    private void deleteFile(File file) {
        if (file != null && file.exists() && !file.delete()) {
            log.warn("无法删除文件: {}", file.getAbsolutePath());
        }
    }
}