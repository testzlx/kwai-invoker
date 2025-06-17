package com.kuaishou.invoker.downgrade;

import com.kuaishou.invoker.ListInvoker;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-03-30
 */
public class ListDowngradeHandler<I, O, T> {

    final ListInvoker<I, O, T> invoker;
    final List<DowngradeHandler<I, O, T>> handlers;

    public ListDowngradeHandler(ListInvoker<I, O, T> invoker, List<DowngradeHandler<I, O, T>> handlers) {
        this.invoker = invoker;
        this.handlers = handlers;
    }

    public ListInvoker<I, O, T> thenAction(Runnable logAction) {
        handlers.forEach(DowngradeHandler::record);
        return invoker;
    }

    public ListInvoker<I, O, T> thenReturn(Supplier<T> returnAction) {
        handlers.forEach(d -> d.thenReturn(returnAction));
        return this.invoker;
    }

    public ListInvoker<I, O, T> thenSkip() {
        handlers.forEach(DowngradeHandler::thenSkip);
        return this.invoker;
    }
}
