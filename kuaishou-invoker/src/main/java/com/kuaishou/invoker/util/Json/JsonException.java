package com.kuaishou.invoker.util.Json;

import org.springframework.core.NestedRuntimeException;

/**
 * @author liuzhifeng <liuzhifeng03@kuaishou.com>
 * Created on 2025-05-09
 */
public class JsonException extends NestedRuntimeException {

    public JsonException(String message) {
        super(message);
    }

    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }

}
