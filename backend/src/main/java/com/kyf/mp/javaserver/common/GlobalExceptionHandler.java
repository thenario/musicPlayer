package com.kyf.mp.javaserver.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResultModel<Void>> handleBusinessException(BusinessException e) {
        HttpStatus status = resolveStatus(e.getCode());
        log.warn("业务异常: status={}, message={}", status.value(), e.getMessage());
        return ResponseEntity.status(status).body(ResultModel.error(e.getMessage(), status.value()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResultModel<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        return badRequest("请求体格式不正确或 Content-Type 不支持");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResultModel<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return badRequest("请求参数类型不匹配");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultModel<Void>> handleException(Exception e) {
        log.error("未处理的系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultModel.error("服务器繁忙，请稍后再试", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    private ResponseEntity<ResultModel<Void>> badRequest(String message) {
        return ResponseEntity.badRequest().body(ResultModel.error(message, HttpStatus.BAD_REQUEST.value()));
    }

    private HttpStatus resolveStatus(Integer code) {
        if (code == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        try { return HttpStatus.valueOf(code); }
        catch (IllegalArgumentException ignored) { return HttpStatus.BAD_REQUEST; }
    }
}
