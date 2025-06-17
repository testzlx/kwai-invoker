package com.kuaishou.invoker;

import com.google.common.util.concurrent.ListenableFuture;
import com.kuaishou.invoker.handler.list.ListExResultHandler;
import com.kuaishou.invoker.async.LazySupplier;
import com.kuaishou.invoker.downgrade.DowngradeHandler;
import com.kuaishou.invoker.downgrade.ExceptionDowngradeInfo;
import com.kuaishou.invoker.downgrade.ListDowngradeHandler;
import com.kuaishou.invoker.handler.ExResultHandler;
import com.kuaishou.invoker.handler.ExceptionHandler;
import com.kuaishou.invoker.handler.ResultHandler;
import com.kuaishou.invoker.handler.list.ListExceptionHandler;
import com.kuaishou.invoker.handler.list.ListResultHandler;
import com.kuaishou.invoker.manager.InvokerCallManager;
import com.kuaishou.invoker.model.ResultErrorInfo;
import org.apache.commons.collections4.ListUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-26
 */
public class ListInvoker<I, O, T> {

    final ListCaller<I, O, T> caller;

    ListInvoker(ListCaller<I, O, T> caller) {
        this.caller = caller;
    }

    public static <IN, OU, REQ> ListCaller<IN, OU, OU> callList(IN instance, BiFunction<IN, REQ, OU> function, List<REQ> requestList) {
        List<Caller<IN, OU, OU>> callers = ListUtils.emptyIfNull(requestList).stream().map(req -> {
            Function<IN, OU> transFunction = in -> function.apply(instance, req);
            return Invoker.call(instance, transFunction, req);
        }).collect(Collectors.toList());
        return new ListCaller<>(callers);
    }

    public static <IN, OU, REQ> ListCaller<IN, OU, OU> callAsyncList(IN instance, BiFunction<IN, REQ, ListenableFuture<OU>> function, List<REQ> requestList, long timeout, TimeUnit unit) {
        List<Caller<IN, OU, OU>> callers = ListUtils.emptyIfNull(requestList).stream().map(req -> {
            Function<IN, ListenableFuture<OU>> transFunction = in -> function.apply(instance, req);
            return Invoker.callAsync(instance, transFunction, timeout, unit, req);
        }).collect(Collectors.toList());
        return new ListCaller<>(callers);
    }

    public <RE> ListInvoker<I, O, RE> processor(Function<O, RE> processor) {
        return Convertor.convertInvokerList(this, processor);
    }

    public ListInvoker<I, O, T> monitor(Monitor.MonitorFactor monitor) {
        caller.callers.forEach(c -> c.invoker.monitor(monitor));
        return this;
    }

    public ListExceptionHandler<I, O, T> whenException() {
        return whenExceptionFail(ex -> true);
    }

    public ListExceptionHandler<I, O, T> whenExceptionFail(Predicate<Throwable> predicate) {
        List<ExceptionHandler<I, O, T>> handlers = caller.callers.stream().map(c -> {
            ExceptionHandler<I, O, T> handler = new ExceptionHandler<>(c.invoker, predicate);
            c.invoker.handler.addHandler(handler);
            return handler;
        }).collect(Collectors.toList());
        return new ListExceptionHandler<>(this, handlers);
    }

    public ListResultHandler<I, O, T> whenResultFail(Predicate<O> predicate) {
        List<ResultHandler<I, O, T>> handlers = caller.callers.stream().map(c -> {
            ResultHandler<I, O, T> handler = new ResultHandler<>(c.invoker, predicate);
            c.invoker.handler.addHandler(handler);
            return handler;
        }).collect(Collectors.toList());
        return new ListResultHandler<>(this, handlers);
    }


    public ListExResultHandler<I, O, T> whenExceptionOrResultFail(Predicate<O> predicate) {
        List<ExResultHandler<I, O, T>> handlers = caller.callers.stream().map(c -> {
            ExResultHandler<I, O, T> handler = new ExResultHandler<>(c.invoker, predicate);
            c.invoker.handler.addHandler(handler);
            return handler;
        }).collect(Collectors.toList());
        return new ListExResultHandler<>(this, handlers);
    }

    public ListInvoker<I, O, T> errorParser(Function<O, ResultErrorInfo> parser) {
        caller.callers.forEach(c -> c.invoker.errorParser(parser));
        return this;
    }

    public ListInvoker<I, O, T> recorder(Recorder recorder) {
        caller.callers.forEach(c -> c.invoker.recorder(recorder));
        return this;
    }

    public ListInvoker<I, O, T> logMethod(BiConsumer<O, Throwable> logMethod) {
        caller.callers.forEach(c -> c.invoker.logMethod(logMethod));
        return this;
    }

    public ListInvoker<I, O, T> defaultValue(Supplier<T> defaultValue) {
        caller.callers.forEach(c -> c.invoker.defaultValue(defaultValue));
        return this;
    }

    public ListDowngradeHandler<I, O, T> whenDowngrade(Supplier<Boolean> condition) {
        List<DowngradeHandler<I, O, T>> handlers = caller.callers.stream().map(c ->
                c.invoker.whenDowngrade(condition)
        ).collect(Collectors.toList());
        return new ListDowngradeHandler<>(this, handlers);
    }

    public List<T> getReturnValue() {
        return caller.invoke(Function.identity());
    }

    public LazySupplier<List<T>> getReturnValueAsync() {
        return caller.invokeAsync(Function.identity());
    }

    public <RES> RES getReturnValue(Function<List<T>, RES> reducer) {
        return caller.invoke(reducer);
    }

    public <RES> LazySupplier<RES> getReturnValueAsync(Function<List<T>, RES> reducer) {
        return caller.invokeAsync(reducer);
    }

    public static List<ExceptionDowngradeInfo> getDowngradeInfoList() {
        return InvokerCallManager.getDowngradeInfoList();
    }
}
