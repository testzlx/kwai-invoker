package com.kuaishou.invoker.util.Json;

import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author liuzhifeng <liuzhifeng03@kuaishou.com>
 * Created on 2025-05-09
 */
public class UncheckedJsonProcessingException extends UncheckedIOException {

    public UncheckedJsonProcessingException(JsonProcessingException cause) {
        super(cause.getMessage(), cause);
    }
}