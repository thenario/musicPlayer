package com.kyf.mp.javaserver.modules.UserModule.serviceImp;

import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.UserModule.DTO.EditUserDTO;
import com.kyf.mp.javaserver.modules.UserModule.VO.EditVO;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;

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
    @Value("${file.static.user-cover-url}")
    private String userCoverUrl;

    @Value("${file.upload.user-cover-path}")
    private String userCoverPath;

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

    @Override
    public ResultModel<Map<String, String>> getUserCoverUrl(Integer userId) {
        Users user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        String userCover = user.getUserCoverUrl();

        if (userCover == null || userCover.trim().isEmpty()) {
            throw new BusinessException(404, "该用户尚未上传封面");
        }

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("userCoverUrl", userCover);

        return ResultModel.success(resultMap);
    }

    @Override
    public ResultModel<EditVO> editUserProfile(EditUserDTO editData, Integer userId) {
        Users oldUser = baseMapper.selectById(userId);
        if (oldUser == null)
            throw new BusinessException(404, "用户不存在");

        Users newUser = new Users();
        newUser.setUserId(userId);
        boolean isChanged = false;

        // 修改用户名
        if (editData.getUser_name() != null && !editData.getUser_name().trim().isEmpty()) {
            newUser.setUserName(editData.getUser_name());
            isChanged = true;
        }

        // 修改封面
        MultipartFile file = editData.getUser_cover();
        if (file != null && !file.isEmpty()) {
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String newFileName = userId + "_" + System.currentTimeMillis() + suffix;

            // 1. 【硬盘操作】清理旧文件 (因为数据库存的是全路径，需要判断或过滤)
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

            // 2. 【硬盘操作】保存新文件到物理路径
            try {
                // 注意：这里必须用 userCoverPath，绝对不能包含 http://
                File dest = new File(userCoverPath + newFileName);
                file.transferTo(dest);

                // 3. 【数据库操作】存入完整的 URL
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

        // 组装 VO 返回
        EditVO vo = new EditVO();
        vo.setUserName(newUser.getUserName() != null ? newUser.getUserName() : oldUser.getUserName());
        // 直接返回数据库里的完整路径
        vo.setUserCoverUrl(newUser.getUserCoverUrl() != null ? newUser.getUserCoverUrl() : oldUser.getUserCoverUrl());

        return ResultModel.success(vo);
    }
}