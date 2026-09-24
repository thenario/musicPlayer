package com.kyf.mp.server.modules.user.repository;

import com.kyf.mp.server.common.repository.BaseRepository;
import com.kyf.mp.server.modules.user.entity.Users;

/** Users 数据访问；业务规则由 Service 层负责。 */
public interface UsersRepository extends BaseRepository<Users> {
    Users findByName(String username);
    long countByName(String username);
    long countByNameOrEmail(String username, String email);
}
