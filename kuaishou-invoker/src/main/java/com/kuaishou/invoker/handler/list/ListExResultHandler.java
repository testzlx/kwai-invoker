package com.kuaishou.invoker.handler.list;

import com.kuaishou.invoker.ListInvoker;
import com.kuaishou.invoker.handler.ExResultHandler;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-01-28
 */
public class ListExResultHandler<I, O, T> {

    private final ListInvoker<I, O, T> invoker;
    private final List<ExResultHandler<I, O, T>> handlers;

    public ListExResultHandler(ListInvoker<I, O, T> invoker, List<ExResultHandler<I, O, T>> handlers) {
        this.invoker = invoker;
        this.handlers = handlers;
    }


    public ListInvoker<I, O, T> thenThrow(Supplier<? extends RuntimeException> throwAction) {
        handlers.forEach(h -> h.thenThrow(throwAction));
        return invoker;
    }

    public ListInvoker<I, O, T> thenReturn(Supplier<T> returnAction) {
        handlers.forEach(h -> h.thenReturn(returnAction));
        return invoker;
    }
}
