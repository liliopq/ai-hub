package com.ai_hub.exception;

import com.ai_hub.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常类
 * 携带 ErrorCode，由 GlobalExceptionHandler 统一捕获并返回正确的业务错误码
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
