package com.kuaishou.invoker.handler;

import com.kuaishou.invoker.Invoker;
import com.kuaishou.invoker.model.TestResult;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-01-28
 */
public class ExceptionHandler<I, O, T> extends CallHandler<I, O, T> {

    private final Predicate<Throwable> predicate;
    private Function<Throwable, ? extends RuntimeException> throwAction;
    private Consumer<Throwable> logAction;
    private Function<Throwable, T> returnAction;

    public ExceptionHandler(Invoker<I, O, T> invoker, Predicate<Throwable> predicate) {
        super(invoker);
        this.predicate = predicate;
    }

    @Override
    public TestResult test(Throwable t, O res) {
        return new TestResult(t != null && predicate.test(t)).onException();
    }

    @Override
    public T action(Throwable t, O res) {
        if (throwAction != null) {
            throw throwAction.apply(t);
        }
        if (returnAction != null) {
            return returnAction.apply(t);
        }
        if (logAction != null) {
            logAction.accept(t);
            return null;
        }
        throw new IllegalArgumentException("no handle action");
    }

    public Invoker<I, O, T> thenThrow(Function<Throwable, ? extends RuntimeException> throwAction) {
        super.strong = true;
        this.throwAction = throwAction;
        return super.invoker;
    }

    public Invoker<I, O, T> thenAction(Consumer<Throwable> logAction) {
        super.strong = false;
        this.logAction = logAction;
        return super.invoker;
    }

    public Invoker<I, O, T> thenReturn(Function<Throwable, T> returnAction) {
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
