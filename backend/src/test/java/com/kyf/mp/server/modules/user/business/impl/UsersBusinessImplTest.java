package com.kyf.mp.server.modules.user.business.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.modules.user.dto.EditUserDTO;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.mapper.UsersMapper;
import com.kyf.mp.server.modules.user.vo.EditVO;

@ExtendWith(MockitoExtension.class)
class UsersBusinessImplTest {

    @Mock
    private UsersMapper usersMapper;

    private UsersBusinessImpl usersBusiness;

    @BeforeEach
    void setUp() {
        if (TableInfoHelper.getTableInfo(Users.class) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), Users.class);
        }
        usersBusiness = new UsersBusinessImpl();
        ReflectionTestUtils.setField(usersBusiness, "baseMapper", usersMapper);
        ReflectionTestUtils.setField(usersBusiness, "userCoverUrl", "/static/user-covers/");
        ReflectionTestUtils.setField(usersBusiness, "userCoverPath", "target/test-user-covers");
    }

    @Test
    @DisplayName("用户不存在时编辑资料抛出404")
    void rejectsMissingUser() {
        when(usersMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> usersBusiness.editUserProfile(new EditUserDTO(), 1L));

        assertThat(exception.getCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("没有修改内容时不更新用户")
    void doesNotUpdateWhenNothingChanged() {
        Users oldUser = user(1L, "old-name");
        oldUser.setUserCoverUrl("/old-cover.jpg");
        when(usersMapper.selectById(1L)).thenReturn(oldUser);

        EditVO result = usersBusiness.editUserProfile(new EditUserDTO(), 1L);

        assertThat(result.getUserName()).isEqualTo("old-name");
        assertThat(result.getUserCoverUrl()).isEqualTo("/old-cover.jpg");
        verify(usersMapper, never()).updateById(any(Users.class));
    }

    @Test
    @DisplayName("修改用户名时更新用户并返回新用户名")
    void updatesUserName() {
        Users oldUser = user(1L, "old-name");
        EditUserDTO dto = new EditUserDTO();
        dto.setUser_name(" new-name ");
        when(usersMapper.selectById(1L)).thenReturn(oldUser);
        when(usersMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        EditVO result = usersBusiness.editUserProfile(dto, 1L);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(usersMapper).updateById(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getUserName()).isEqualTo("new-name");
        assertThat(result.getUserName()).isEqualTo("new-name");
    }

    @Test
    @DisplayName("用户名已被占用时抛出409")
    void rejectsTakenUserName() {
        Users oldUser = user(1L, "old-name");
        EditUserDTO dto = new EditUserDTO();
        dto.setUser_name("new-name");
        when(usersMapper.selectById(1L)).thenReturn(oldUser);
        when(usersMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> usersBusiness.editUserProfile(dto, 1L));

        assertThat(exception.getCode()).isEqualTo(409);
        verify(usersMapper, never()).updateById(any(Users.class));
    }

    private Users user(Long id, String name) {
        Users user = new Users();
        user.setUserId(id);
        user.setUserName(name);
        return user;
    }
}
