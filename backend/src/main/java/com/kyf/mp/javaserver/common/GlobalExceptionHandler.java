package com.kyf.mp.javaserver.common;

import java.util.List;
import java.util.Objects;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResultModel<Void>> handleBusinessException(BusinessException e) {
        HttpStatus status = resolveStatus(e.getCode());
        log.warn("业务异常: status={}, message={}", status.value(), e.getMessage());
        return ResponseEntity.status(status).body(ResultModel.error(e.getMessage(), status.value()));
    }

    // ==================== 参数校验异常（Bean Validation 注解） ====================

    // @RequestBody @Valid 校验失败（MethodArgumentNotValidException 是其子类）
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResultModel<Void>> handleBindException(BindException e) {
        return validationError(firstErrorMessage(e.getAllErrors()));
    }

    // 方法参数校验失败（控制器 @Validated + @RequestParam/@PathVariable/@RequestAttribute 上的约束注解）
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ResultModel<Void>> handleMethodValidation(HandlerMethodValidationException e) {
        return validationError(firstErrorMessage(e.getAllErrors()));
    }

    // 非 MVC 路径校验失败（如 @Validated Service 方法）
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResultModel<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("参数校验失败");
        return validationError(message);
    }

    // ==================== 请求解析与参数绑定异常（框架层） ====================

    // 缺少必填的 @RequestParam / multipart part
    @ExceptionHandler({ MissingServletRequestParameterException.class, MissingServletRequestPartException.class })
    public ResponseEntity<ResultModel<Void>> handleMissingParam(Exception e) {
        return validationError("缺少必要的请求参数");
    }

    // 请求体缺失、格式非法或 Content-Type 不支持
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResultModel<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        return validationError("请求体格式不正确或 Content-Type 不支持");
    }

    // 请求参数无法转换成方法参数类型
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResultModel<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return validationError("请求参数类型不匹配");
    }

    // ==================== 兜底异常 ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultModel<Void>> handleException(Exception e) {
        log.error("未处理的系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultModel.error("服务器繁忙，请稍后再试", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    // ==================== 私有工具方法 ====================

    private ResponseEntity<ResultModel<Void>> validationError(String message) {
        log.warn("参数校验失败: {}", message);
        return ResponseEntity.badRequest().body(ResultModel.error(message, HttpStatus.BAD_REQUEST.value()));
    }

    private String firstErrorMessage(List<? extends MessageSourceResolvable> errors) {
        return errors.stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("参数校验失败");
    }

    private HttpStatus resolveStatus(Integer code) {
        if (code == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        try { return HttpStatus.valueOf(code); }
        catch (IllegalArgumentException ignored) { return HttpStatus.BAD_REQUEST; }
    }
}
