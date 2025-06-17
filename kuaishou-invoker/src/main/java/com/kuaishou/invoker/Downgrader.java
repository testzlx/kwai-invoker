package com.kuaishou.invoker;

import java.util.ArrayList;
import java.util.List;

import com.kuaishou.invoker.downgrade.DowngradeHandler;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-01-28
 */
public class Downgrader<I, O, T> {

    Invoker<I, O, T> invoker;
    List<DowngradeHandler<I, O, T>> handlers = new ArrayList<>();

    public Downgrader(Invoker<I, O, T> invoker) {
        this.invoker = invoker;
    }

    public DowngradeHandler<I, O, T> addHandler(DowngradeHandler<I, O, T> handler) {
        this.handlers.add(handler);
        return handler;
    }

    public DowngradeHandler<I, O, T> matchHandler() {
        return handlers.stream().filter(DowngradeHandler::test).findFirst().orElse(null);
    }
}
