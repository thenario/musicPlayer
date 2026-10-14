package com.kyf.mp.server.common;

import java.util.List;
import java.util.Objects;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.apache.catalina.connector.ClientAbortException;

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
        // 可预期的业务拒绝只记录摘要；系统异常才需要完整堆栈。
        if (status.is5xxServerError()) {
            log.error("业务处理失败: code={}, status={}", e.getCode(), status.value(), e);
        } else {
            log.warn("业务异常: status={}, message={}", status.value(), e.getMessage());
        }
        return ResponseEntity.status(status).body(ResultModel.error(e.getMessage(), status.value()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResultModel<Void>> handleAccessDenied(AccessDeniedException exception) {
        log.warn("访问被拒绝");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultModel.error("没有访问权限", HttpStatus.FORBIDDEN.value()));
    }
    // ==================== 参数校验异常（Bean Validation 注解） ====================

    // @RequestBody @Valid 校验失败（MethodArgumentNotValidException 是其子类）
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResultModel<Void>> handleBindException(BindException e) {
        return validationError(firstErrorMessage(e.getAllErrors()));
    }

    // Spring MVC 内建方法校验；类级 @Validated 通常走 AOP 校验。
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ResultModel<Void>> handleMethodValidation(HandlerMethodValidationException e) {
        // 返回值违反约束是服务端错误，不能归咎于客户端参数。
        if (e.isForReturnValue()) {
            return handleException(e);
        }
        return validationError(firstErrorMessage(e.getAllErrors()));
    }

    // 非 MVC 路径校验失败（如 @Validated Service 方法）
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResultModel<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(Objects::nonNull)
                .filter(messageText -> !messageText.isBlank())
                .sorted()
                .findFirst()
                .orElse("参数校验失败");
        return validationError(message);
    }

    // 缺少必填的 @RequestParam / multipart part
    @ExceptionHandler({ MissingServletRequestParameterException.class, MissingServletRequestPartException.class })
    public ResponseEntity<ResultModel<Void>> handleMissingParam(Exception e) {
        return validationError("缺少必要的请求参数");
    }

    // 请求体缺失或 JSON 无法解析；媒体类型不支持由 ErrorResponse 分支保留 415。
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResultModel<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        return validationError("请求体缺失或格式不正确");
    }

    // 请求参数无法转换成方法参数类型
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResultModel<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return validationError("请求参数类型不匹配");
    }

    // 音频播放、切歌或拖动进度时，浏览器可能主动取消旧的 Range 请求。
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbort(ClientAbortException e) {
        log.debug("客户端中止了资源连接");
    }

    // 依赖服务不可用与登录失效不同，前端收到 503 时应保留现有登录状态。
    @ExceptionHandler({ DataAccessResourceFailureException.class, QueryTimeoutException.class })
    public ResponseEntity<ResultModel<Void>> handleDependencyUnavailable(Exception e) {
        log.error("依赖服务连接失败或请求超时", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ResultModel.error("依赖服务暂不可用，请稍后重试", HttpStatus.SERVICE_UNAVAILABLE.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultModel<Void>> handleException(Exception e) {
        // 保留 Spring 定义的状态码和响应头，例如 405 的 Allow、415、413、404。
        // 响应只使用安全文案，不暴露异常中可能包含的内部详情。
        if (e instanceof ErrorResponse errorResponse) {
            int status = errorResponse.getStatusCode().value();
            if (errorResponse.getStatusCode().is5xxServerError()) {
                log.error("请求处理失败: status={}", status, e);
            } else {
                log.warn("请求被拒绝: status={}, type={}", status, e.getClass().getSimpleName());
            }
            return ResponseEntity.status(errorResponse.getStatusCode())
                    .headers(errorResponse.getHeaders())
                    .body(ResultModel.error(frameworkErrorMessage(status), status));
        }
        log.error("未处理的系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultModel.error("服务器繁忙，请稍后再试", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    private ResponseEntity<ResultModel<Void>> validationError(String message) {
        log.warn("参数校验失败: {}", message);
        return ResponseEntity.badRequest().body(ResultModel.error(message, HttpStatus.BAD_REQUEST.value()));
    }

    private String firstErrorMessage(List<? extends MessageSourceResolvable> errors) {
        return errors.stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .filter(message -> !message.isBlank())
                .sorted()
                .findFirst()
                .orElse("参数校验失败");
    }

    private HttpStatus resolveStatus(Integer code) {
        HttpStatus status = code == null ? null : HttpStatus.resolve(code);
        // 异常不能返回 2xx/3xx；无效状态码属于服务端编程错误。
        return status != null && status.isError() ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String frameworkErrorMessage(int status) {
        return switch (status) {
            case 400 -> "请求参数不正确";
            case 401 -> "请先登录";
            case 403 -> "没有访问权限";
            case 404 -> "请求的资源不存在";
            case 405 -> "不支持该请求方法";
            case 406 -> "不支持请求的响应格式";
            case 413 -> "上传内容超过大小限制";
            case 415 -> "不支持该请求的 Content-Type";
            case 416 -> "请求的资源范围无效";
            case 429 -> "请求过于频繁，请稍后重试";
            case 503 -> "依赖服务暂不可用，请稍后重试";
            default -> status >= 500 ? "服务器繁忙，请稍后再试" : "请求无法处理";
        };
    }
}
