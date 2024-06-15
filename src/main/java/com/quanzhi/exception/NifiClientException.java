package com.quanzhi.exception;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/6/14
 * @Copyright: https://github.com/CatTailzz
 */
public class NifiClientException extends RuntimeException{

    public NifiClientException(String message) {
        super(message);
    }

    public NifiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
