package com.kyf.mp.server.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("业务异常状态码合法时，应保留原状态码和消息")
    void preservesValidBusinessStatus() {
        ResponseEntity<ResultModel<Void>> response =
                handler.handleBusinessException(new BusinessException(403, "没有访问权限"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
                .extracting(ResultModel::getCode, ResultModel::getMessage)
                .containsExactly(403, "没有访问权限");
    }

    @Test
    @DisplayName("业务异常状态码非法时，应映射为 400")
    void mapsInvalidBusinessStatusToBadRequest() {
        ResponseEntity<ResultModel<Void>> response =
                handler.handleBusinessException(new BusinessException(999, "无效状态"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("未处理异常时，不应暴露内部错误详情")
    void hidesUnhandledExceptionDetails() {
        ResponseEntity<ResultModel<Void>> response =
                handler.handleException(new IllegalStateException("sensitive database detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .extracting(ResultModel::getCode, ResultModel::getMessage)
                .containsExactly(500, "服务器繁忙，请稍后再试");
    }
}
