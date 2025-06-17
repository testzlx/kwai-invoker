package com.kuaishou.invoker.handler.list;

import com.kuaishou.invoker.ListInvoker;
import com.kuaishou.invoker.handler.ExceptionHandler;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-01-28
 */
public class ListExceptionHandler<I, O, T> {

    private final ListInvoker<I, O, T> invoker;
    private final List<ExceptionHandler<I, O, T>> handlers;


    public ListExceptionHandler(ListInvoker<I, O, T> invoker, List<ExceptionHandler<I, O, T>> handlers) {
        this.invoker = invoker;
        this.handlers = handlers;
    }

    public ListInvoker<I, O, T> thenThrow(Function<Throwable, ? extends RuntimeException> throwAction) {
        handlers.forEach(h -> h.thenThrow(throwAction));
        return invoker;
    }

    public ListInvoker<I, O, T> thenAction(Consumer<Throwable> logAction) {
        handlers.forEach(h -> h.thenAction(logAction));
        return invoker;
    }

    public ListInvoker<I, O, T> thenReturn(Function<Throwable, T> returnAction) {
        handlers.forEach(h -> h.thenReturn(returnAction));
        return invoker;
    }

    public ListInvoker<I, O, T> thenSkip() {
        handlers.forEach(ExceptionHandler::thenSkip);
        return invoker;
    }
}
