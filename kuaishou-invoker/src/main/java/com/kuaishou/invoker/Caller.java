package com.kuaishou.invoker;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import com.ecyrd.speed4j.StopWatch;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.kuaishou.invoker.async.AsyncRpcCallManager;
import com.kuaishou.invoker.async.GrpcCall;
import com.kuaishou.invoker.async.GrpcCall.ThrowableSupplier;
import com.kuaishou.invoker.async.LazySupplier;
import com.kuaishou.invoker.downgrade.DowngradeHandler;
import com.kuaishou.invoker.model.CallType;
import com.kuaishou.invoker.model.HandleInfo;
import com.kuaishou.invoker.model.StrongType;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-11
 */
@Slf4j
public class Caller<I, O, T> {
    I instance;
    Function<I, O> function;
    AsyncFunction<I, O> asyncFunction;
    Supplier<O> staticFunction;
    AsyncFunction<?, O> asyncStaticFunction;
    Function<O, T> processor;
    Invoker<I, O, T> invoker;
    Object[] reqArgs;
    CallMode callMode;


    Caller(I instance, Function<I, O> function, AsyncFunction<I, O> asyncFunction, Supplier<O> staticFunction, AsyncFunction<?, O> asyncStaticFunction, Object[] reqArgs) {
        this(instance, function, asyncFunction, staticFunction, asyncStaticFunction, reqArgs, res -> (T) res, CallMode.SYNC);
    }

    Caller(I instance, Function<I, O> function, AsyncFunction<I, O> asyncFunction, Supplier<O> staticFunction, AsyncFunction<?, O> asyncStaticFunction,
           Object[] reqArgs, Function<O, T> processor, CallMode callMode) {
        this.instance = instance;
        this.function = function;
        this.reqArgs = reqArgs;
        this.asyncFunction = asyncFunction;
        this.staticFunction = staticFunction;
        this.asyncStaticFunction = asyncStaticFunction;
        this.processor = processor;
        this.callMode = callMode;
        this.invoker = new Invoker<>(this);
    }

    public Invoker<I, O, T> desc(CallType type, String name) {
        this.invoker.desc = new CallDesc(type, name);
        return this.invoker;
    }

    public Invoker<I, O, T> desc(CallType type, String name, String domain) {
        this.invoker.desc = new CallDesc(type, name, domain);
        return this.invoker;
    }

    public Invoker<I, O, T> desc(CallType type, String name, StrongType strongType) {
        this.invoker.desc = new CallDesc(type, name, strongType);
        return this.invoker;
    }

    public Invoker<I, O, T> desc(CallType type, String name, String domain, StrongType strongType) {
        this.invoker.desc = new CallDesc(type, name, domain, strongType);
        return this.invoker;
    }

    T invoke() {
        DowngradeHandler<I, O, T> handler = invoker.downgrader.matchHandler();
        if (handler != null) {
            return actionResOrDefaultValue(handler);
        }
        Preconditions.checkArgument(function != null, "同步方法未设置，无法使用getReturnValue");
        invoker.monitor.monitorReq();
        return invokeAndProcess(() -> function.apply(instance));
    }

    public void invokeVoid() {
        invoke();
    }

    T invokeStatic() {
        DowngradeHandler<I, O, T> handler = invoker.downgrader.matchHandler();
        if (handler != null) {
            return actionResOrDefaultValue(handler);
        }
        Preconditions.checkArgument(staticFunction != null, "静态方法未设置，无法使用getReturnValueStatic");
        invoker.monitor.monitorReq();
        return invokeAndProcess(() -> staticFunction.get());
    }

    LazySupplier<T> invokeAsync() {
        DowngradeHandler<I, O, T> handler = invoker.downgrader.matchHandler();
        if (handler != null) {
            return LazySupplier.of(actionResOrDefaultValue(handler));
        }
        Preconditions.checkArgument(asyncFunction != null, "异步方法未设置，无法使用getReturnValueAsync");
        Function<I, ListenableFuture<O>> futureFunction = asyncFunction.futureFunction;
        Preconditions.checkArgument(futureFunction != null, "异步方法未设置，无法使用getReturnValueAsync");
        this.callMode = CallMode.ASYNC;
        Supplier<ListenableFuture<O>> futureSupplier = () -> futureFunction.apply(instance);
        invoker.monitor.monitorReq();
        return GrpcCall.<O, T> newBuilder()
                .async(futureSupplier, asyncFunction.timeout, asyncFunction.timeUnit)
                .processor(this::invokeAndProcess)
                .listener(() -> {
                    StopWatch stopWatch = invoker.monitor.stopWatch;
                    if (stopWatch != null) {
                        stopWatch.stop();
                    }
                }).name(invoker.desc.name)
                .withExecutionUnit(AsyncRpcCallManager.withExecutionUnit()).build().asyncCall();
    }

    LazySupplier<T> invokeStaticAsync() {
        DowngradeHandler<I, O, T> handler = invoker.downgrader.matchHandler();
        if (handler != null) {
            return LazySupplier.of(actionResOrDefaultValue(handler));
        }
        Preconditions.checkArgument(asyncStaticFunction != null, "异步方法未设置，无法使用getReturnValueStaticAsync");
        Supplier<ListenableFuture<O>> futureFunctionStatic = asyncStaticFunction.futureFunctionStatic;
        Preconditions.checkArgument(futureFunctionStatic != null, "异步方法未设置，无法使用getReturnValueStaticAsync");
        this.callMode = CallMode.ASYNC;
        invoker.monitor.monitorReq();
        return GrpcCall.<O, T> newBuilder()
                .async(futureFunctionStatic, asyncStaticFunction.timeout, asyncStaticFunction.timeUnit)
                .processor(this::invokeAndProcess)
                .listener(() -> {
                    StopWatch stopWatch = invoker.monitor.stopWatch;
                    if (stopWatch != null) {
                        stopWatch.stop();
                    }
                }).name(invoker.desc.name)
                .withExecutionUnit(AsyncRpcCallManager.withExecutionUnit()).build().asyncCall();
    }

    private T invokeAndProcess(ThrowableSupplier<O> supplier) {
        O res = null;
        Throwable t = null;
        try {
            res = supplier.get();
        } catch (Throwable th) {
            t = th;
        }
        HandleInfo handleInfo = new HandleInfo();
        try {
            return this.invoker.handler.handleResult(t, res, handleInfo);
        } finally {
            try {
                invoker.monitor.monitorResult(t, res, handleInfo);
            } catch (Throwable mt) {
                log.error("[fail-sale] monitorResult exception", mt);
            }
        }
    }

    public T actionResOrDefaultValue(DowngradeHandler<I, O, T> handler) {
        T actionRes = handler.action();
        if (actionRes == null && invoker.defaultValue != null) {
            return invoker.defaultValue.get();
        }
        return actionRes;
    }

    @Getter
    public static class CallDesc {
        private final String name;
        private final CallType type;
        private String scene;
        private StrongType strongType;

        public CallDesc(CallType type, String name) {
            this.name = name;
            this.type = type;
        }

        public CallDesc(CallType type, String name, String scene) {
            this.name = name;
            this.type = type;
            this.scene = scene;
        }

        public CallDesc(CallType type, String name, StrongType strongType) {
            this.name = name;
            this.type = type;
            this.strongType = strongType;
        }

        public CallDesc(CallType type, String name, String scene, StrongType strongType) {
            this.name = name;
            this.type = type;
            this.scene = scene;
            this.strongType = strongType;
        }
    }

    public enum CallMode {
        SYNC,
        ASYNC;
    }

    public static class AsyncFunction<I, O> {
        Function<I, ListenableFuture<O>> futureFunction;

        Supplier<ListenableFuture<O>> futureFunctionStatic;
        long timeout;
        TimeUnit timeUnit;

        public AsyncFunction(Function<I, ListenableFuture<O>> futureFunction, long timeout, TimeUnit timeUnit) {
            this.futureFunction = futureFunction;
            this.timeout = timeout;
            this.timeUnit = timeUnit;
        }

        public AsyncFunction(Supplier<ListenableFuture<O>> futureFunctionStatic, long timeout, TimeUnit timeUnit) {
            this.futureFunctionStatic = futureFunctionStatic;
            this.timeout = timeout;
            this.timeUnit = timeUnit;
        }
    }
}
