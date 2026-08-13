package com.kyf.mp.javaserver.common.business;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 数据访问层基接口：继承 MyBatis-Plus IService，提供基础 CRUD。
 * 简单操作（getById/list/save/removeById/lambdaQuery 等）直接用它，复杂操作在各业务接口中声明。
 *
 * @param <T> 实体类型
 */
public interface BaseBusiness<T> extends IService<T> {
}
