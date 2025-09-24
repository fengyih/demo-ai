package com.tcs.exception;

/**
 * 资源未找到异常，用于表示请求的资源不存在的情况
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}