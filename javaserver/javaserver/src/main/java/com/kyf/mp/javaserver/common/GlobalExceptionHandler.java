package com.kyf.mp.javaserver.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResultModel<Void> handleBusinessException(BusinessException e) {
        log.warn("业务逻辑异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResultModel.error(e.getMessage(), e.getCode());
    }

    @ExceptionHandler(Exception.class)
    public ResultModel<Void> handleException(Exception e) {
        log.error("系统运行崩溃: ", e);
        return ResultModel.error("服务器繁忙，请稍后再试", 500);
    }
}
