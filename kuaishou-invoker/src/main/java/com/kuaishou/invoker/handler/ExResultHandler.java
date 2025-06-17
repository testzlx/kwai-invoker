package com.kuaishou.invoker.handler;

import java.util.function.Predicate;
import java.util.function.Supplier;

import com.kuaishou.invoker.Invoker;
import com.kuaishou.invoker.model.TestResult;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-01-28
 */
public class ExResultHandler<I, O, T> extends CallHandler<I, O, T> {

    private final Predicate<O> predicate;
    private Supplier<? extends RuntimeException> throwAction;
    private Supplier<T> returnAction;

    public ExResultHandler(Invoker<I, O, T> invoker, Predicate<O> predicate) {
        super(invoker);
        this.predicate = predicate;
    }

    @Override
    public TestResult test(Throwable t, O res) {
        if (t != null) {
            return new TestResult(true).onException();
        }
        return new TestResult(predicate.test(res)).onFail();
    }

    @Override
    public T action(Throwable t, O res) {
        if (throwAction != null) {
            throw throwAction.get();
        }
        if (returnAction != null) {
            return returnAction.get();
        }
        throw new IllegalArgumentException("no handle action");
    }

    public Invoker<I, O, T> thenThrow(Supplier<? extends RuntimeException> throwAction) {
        super.strong = true;
        this.throwAction = throwAction;
        return super.invoker;
    }

    public Invoker<I, O, T> thenReturn(Supplier<T> returnAction) {
        super.strong = false;
        this.returnAction = returnAction;
        return super.invoker;
    }
}
