package com.kyf.mp.javaserver.common;

import lombok.Data;

@Data
public class ResultModel<T> {
    private Integer code;
    private String message;
    private T data;

    public ResultModel() {
    }

    public ResultModel(Integer code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    public static <T> ResultModel<T> success(T data) {
        return new ResultModel<>(200, "操作成功", data);
    }

    public static <T> ResultModel<T> error(String msg, Integer code) {
        return new ResultModel<>(code, msg, null);
    }
}
