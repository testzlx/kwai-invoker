package com.kuaishou.invoker.concurrent;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author liuzhuo
 * Created on 2023-03-30 下午8:12
 * callable wrap of Invoker
 * <p>
 * {@link InvokerRunnable}
 */
public final class InvokerCallable<V> implements Callable<V> {

    private final InvokerContext context;

    private final Callable<V> callable;

    @Override
    public V call() throws Exception {
        try {
            context.setCurrentContext();
            return callable.call();
        } finally {
            context.removeCurrentContext();
        }
    }

    private InvokerCallable(Callable<V> callable) {
        this.context = InvokerContext.getCurrentContext();
        this.callable = callable;
    }

    public static <T> InvokerCallable<T> wrap(Callable<T> callable) {
        return new InvokerCallable<>(callable);
    }

    public static <T> List<InvokerCallable<T>> wraps(Collection<? extends Callable<T>> callables) {
        if (Objects.isNull(callables)) {
            return Collections.emptyList();
        }

        List<InvokerCallable<T>> r = Lists.newArrayList();
        for (Callable<T> callable : callables) {
            r.add(InvokerCallable.wrap(callable));
        }
        return r;
    }
}
