package com.kyf.mp.server.modules.user.repository.impl;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyf.mp.server.common.repository.BaseRepositoryImpl;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.mapper.UsersMapper;
import com.kyf.mp.server.modules.user.repository.UsersRepository;

/** Users 数据访问实现。 */
@Repository
public class UsersRepositoryImpl extends BaseRepositoryImpl<UsersMapper, Users> implements UsersRepository {

    public UsersRepositoryImpl(UsersMapper mapper) {
        super(mapper);
    }

    @Override
    public Users findByName(String username) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Users>().eq(Users::getUserName, username));
    }

    @Override
    public long countByName(String username) {
        return baseMapper.selectCount(new LambdaQueryWrapper<Users>().eq(Users::getUserName, username));
    }

    @Override
    public long countByNameOrEmail(String username, String email) {
        return baseMapper.selectCount(new LambdaQueryWrapper<Users>()
                .eq(Users::getUserName, username).or().eq(Users::getUserEmail, email));
    }
}
