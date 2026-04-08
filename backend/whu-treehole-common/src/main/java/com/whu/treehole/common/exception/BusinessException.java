package com.whu.treehole.common.exception;

/* 业务异常用于承载明确的错误码和用户可理解的信息。 */

public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
