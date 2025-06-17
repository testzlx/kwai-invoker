package com.kuaishou.invoker.handler;

import com.kuaishou.invoker.Invoker;
import com.kuaishou.invoker.model.TestResult;

import lombok.Getter;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-01-28
 */
public abstract class CallHandler<I, O, T> {

    protected Invoker<I, O, T> invoker;
    @Getter
    protected boolean strong;

    public CallHandler(Invoker<I, O, T> invoker) {
        this.invoker = invoker;
    }

    public abstract TestResult test(Throwable t, O res);

    public abstract T action(Throwable t, O res);
}
