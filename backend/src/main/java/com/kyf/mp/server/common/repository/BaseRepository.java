package com.kyf.mp.server.common.repository;

import java.io.Serializable;
import java.util.Collection;

/**
 * 数据访问公共接口。只暴露记录读写，不承载权限、HTTP、文件或业务流程。
 * 条件查询由具体 Repository 提供具名方法，Wrapper 留在实现内部。
 */
public interface BaseRepository<T> {
    T getById(Serializable id);
    boolean save(T entity);
    boolean updateById(T entity);
    int insert(T entity);
    int deleteById(Serializable id);
    int deleteBatchIds(Collection<?> ids);
}
