package com.kyf.mp.server.common.repository;

import java.io.Serializable;
import java.util.Collection;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/** 复用 MyBatis-Plus CRUD；通过窄接口隔离上层与条件构造器。 */
public abstract class BaseRepositoryImpl<M extends BaseMapper<T>, T>
        extends ServiceImpl<M, T> implements BaseRepository<T> {
    protected BaseRepositoryImpl(M mapper) {
        this.baseMapper = mapper;
    }

    @Override
    public T getById(Serializable id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean save(T entity) {
        return baseMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(T entity) {
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public int insert(T entity) {
        return baseMapper.insert(entity);
    }

    @Override
    public int deleteById(Serializable id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public int deleteBatchIds(Collection<?> ids) {
        return baseMapper.deleteBatchIds(ids);
    }
}
