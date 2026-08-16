package com.kyf.mp.server.modules.user.business.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(rollbackFor = Exception.class)
    public EditVO editUserProfile(EditUserDTO editData, Long userId) {
        Users oldUser = getExistingUser(userId);
        Users newUser = new Users();
        newUser.setUserId(userId);
        boolean isChanged = applyUserNameChange(editData, oldUser, newUser);
        File savedCoverFile = saveCoverIfPresent(editData.getUser_cover(), userId, newUser);
        isChanged = isChanged || savedCoverFile != null;
        updateUser(newUser, isChanged, savedCoverFile);
        cleanupSavedCoverHistory(savedCoverFile, userId);
        return buildEditResult(oldUser, newUser);
    }

    private Users getExistingUser(Long userId) {
        Users user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private boolean applyUserNameChange(EditUserDTO editData, Users oldUser, Users newUser) {
        String requestedName = editData.getUser_name();
        if (requestedName == null || requestedName.trim().isEmpty()) {
            return false;
        }
        requestedName = requestedName.trim();
        ensureUserNameAvailable(requestedName, oldUser.getUserName());
        newUser.setUserName(requestedName);
        return true;
    }

    private void ensureUserNameAvailable(String requestedName, String currentName) {
        boolean nameTaken = !requestedName.equals(currentName)
                && baseMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Users>()
                        .eq(Users::getUserName, requestedName)) > 0;
        if (nameTaken) {
            throw new BusinessException(409, "用户名已被使用");
        }
    }

    private File saveCoverIfPresent(MultipartFile file, Long userId, Users newUser) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String extension = UploadFileValidator.validateImage(file);
        File directory = prepareCoverDirectory();
        File savedFile = new File(directory, userId + "_" + System.currentTimeMillis() + "." + extension);
        try {
            file.transferTo(savedFile);
            newUser.setUserCoverUrl(userCoverUrl + savedFile.getName());
            return savedFile;
        } catch (IOException exception) {
            deleteFile(savedFile);
            log.error("用户ID: {} 封面写入硬盘失败", userId, exception);
            throw new BusinessException(500, "文件保存失败");
        }
    }

    private File prepareCoverDirectory() {
        File directory = new File(userCoverPath);
        if (!directory.exists()) {
            try {
                Files.createDirectories(directory.toPath());
            } catch (IOException exception) {
                throw new BusinessException(500, "头像目录创建失败");
            }
        }
        return directory;
    }

    private void updateUser(Users newUser, boolean isChanged, File savedCoverFile) {
        if (!isChanged) {
            return;
        }
        try {
            baseMapper.updateById(newUser);
        } catch (DuplicateKeyException exception) {
            deleteFile(savedCoverFile);
            throw new BusinessException(409, "用户名已被使用");
        } catch (RuntimeException exception) {
            deleteFile(savedCoverFile);
            throw exception;
        }
    }

    private void cleanupSavedCoverHistory(File savedCoverFile, Long userId) {
        if (savedCoverFile != null) {
            cleanupPreviousCovers(savedCoverFile.getParentFile(), userId, savedCoverFile.getName());
        }
    }

    private EditVO buildEditResult(Users oldUser, Users newUser) {
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
        if (file == null || !file.exists()) {
            return;
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException exception) {
            log.warn("Unable to delete file: {}", file.getAbsolutePath(), exception);
        }
    }
}