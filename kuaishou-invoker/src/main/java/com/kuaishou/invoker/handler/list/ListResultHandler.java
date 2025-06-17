package com.kuaishou.invoker.handler.list;

import com.kuaishou.invoker.ListInvoker;
import com.kuaishou.invoker.handler.ResultHandler;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-01-28
 */
public class ListResultHandler<I, O, T> {

    private final ListInvoker<I, O, T> invoker;
    private final List<ResultHandler<I, O, T>> handlers;


    public ListResultHandler(ListInvoker<I, O, T> invoker, List<ResultHandler<I, O, T>> handlers) {
        this.invoker = invoker;
        this.handlers = handlers;
    }

    public ListInvoker<I, O, T> thenThrow(Function<O, ? extends RuntimeException> throwAction) {
        handlers.forEach(h -> h.thenThrow(throwAction));
        return invoker;
    }

    public ListInvoker<I, O, T> thenAction(Consumer<O> logAction) {
        handlers.forEach(h -> h.thenAction(logAction));
        return invoker;
    }

    public ListInvoker<I, O, T> thenReturn(Function<O, T> returnAction) {
        handlers.forEach(h -> h.thenReturn(returnAction));
        return invoker;
    }

    public ListInvoker<I, O, T> thenSkip() {
        handlers.forEach(ResultHandler::thenSkip);
        return invoker;
    }
}
