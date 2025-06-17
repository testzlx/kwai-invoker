package com.kuaishou.invoker.concurrent;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author liuzhuo
 * Created on 2023-04-03 下午2:06
 * Executor包装
 *  {@link InvokerCallable}
 *  {@link InvokerRunnable}
 */
public class ExecutorInvokerWrapper implements Executor {

    private final Executor executor;

    public ExecutorInvokerWrapper(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(InvokerRunnable.wrap(command));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecutorInvokerWrapper that = (ExecutorInvokerWrapper) o;
        return Objects.equals(executor, that.executor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executor);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + executor;
    }
}
