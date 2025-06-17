package com.kuaishou.invoker;

import java.util.List;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-01-28
 */
public class ListDowngrader<I, O, T> {

    private final ListInvoker<I, O, T> invoker;
    private final List<Downgrader<I, O, T>> downgraders;

    public ListDowngrader(ListInvoker<I, O, T> invoker, List<Downgrader<I, O, T>> downgraders) {
        this.invoker = invoker;
        this.downgraders = downgraders;
    }
}
