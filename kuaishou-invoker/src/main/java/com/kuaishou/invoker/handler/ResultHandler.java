package com.kuaishou.invoker.handler;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.kuaishou.invoker.Invoker;
import com.kuaishou.invoker.model.TestResult;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-01-28
 */
public class ResultHandler<I, O, T> extends CallHandler<I, O, T> {

    private final Predicate<O> predicate;
    private Function<O, ? extends RuntimeException> throwAction;
    private Consumer<O> logAction;
    private Function<O, T> returnAction;


    public ResultHandler(Invoker<I, O, T> invoker, Predicate<O> predicate) {
        super(invoker);
        this.predicate = predicate;
    }

    @Override
    public TestResult test(Throwable t, O res) {
        return new TestResult(predicate.test(res)).onFail();
    }

    @Override
    public T action(Throwable t, O res) {
        if (throwAction != null) {
            throw throwAction.apply(res);
        }
        if (returnAction != null) {
            return returnAction.apply(res);
        }
        if (logAction != null) {
            logAction.accept(res);
            return null;
        }
        throw new IllegalArgumentException("no handle action");
    }

    public Invoker<I, O, T> thenThrow(Function<O, ? extends RuntimeException> throwAction) {
        super.strong = true;
        this.throwAction = throwAction;
        return super.invoker;
    }

    public Invoker<I, O, T> thenAction(Consumer<O> logAction) {
        super.strong = false;
        this.logAction = logAction;
        return super.invoker;
    }

    public Invoker<I, O, T> thenReturn(Function<O, T> returnAction) {
        super.strong = false;
        this.returnAction = returnAction;
        return super.invoker;
    }

    public Invoker<I, O, T> thenSkip() {
        super.strong = false;
        this.logAction = a -> {
        };
        return super.invoker;
    }
}
