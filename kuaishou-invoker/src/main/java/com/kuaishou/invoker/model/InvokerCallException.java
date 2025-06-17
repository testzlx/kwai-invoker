package com.kuaishou.invoker.model;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-11
 */
public class InvokerCallException extends RuntimeException {

    public InvokerCallException() {
    }

    public InvokerCallException(String message) {
        super(message);
    }

    public InvokerCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvokerCallException(Throwable cause) {
        super(cause);
    }

    public InvokerCallException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
