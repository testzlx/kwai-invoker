package com.kuaishou.invoker.downgrade;

import com.kuaishou.invoker.Invoker;

import java.util.function.Supplier;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-03-30
 */
public abstract class DowngradeHandler<I, O, T> {

    final Invoker<I, O, T> invoker;
    Runnable logAction;
    Supplier<T> returnAction;
    final Supplier<Boolean> condition;

    public DowngradeHandler(Invoker<I, O, T> invoker, Supplier<Boolean> condition) {
        this.invoker = invoker;
        this.condition = condition;
    }

    public abstract void record();

    public boolean test() {
        return condition.get();
    }

    public T action() {
        this.record();
        if (returnAction != null) {
            return returnAction.get();
        }
        if (logAction != null) {
            logAction.run();
            return null;
        }
        throw new IllegalArgumentException("downgrade no handle action");
    }

    public Invoker<I, O, T> thenAction(Runnable logAction) {
        this.logAction = logAction;
        return this.invoker;
    }

    public Invoker<I, O, T> thenReturn(Supplier<T> returnAction) {
        this.returnAction = returnAction;
        return this.invoker;
    }

    public Invoker<I, O, T> thenSkip() {
        this.logAction = () -> {
        };
        return this.invoker;
    }
}
