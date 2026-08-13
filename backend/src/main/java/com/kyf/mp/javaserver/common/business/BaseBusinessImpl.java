package com.kyf.mp.javaserver.common.business;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 数据访问层基实现：继承 MyBatis-Plus ServiceImpl，获得 baseMapper 与基础 CRUD 能力。
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 */
public abstract class BaseBusinessImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T>
        implements BaseBusiness<T> {
}
