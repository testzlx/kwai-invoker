package com.kuaishou.invoker;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.google.common.util.concurrent.ListenableFuture;
import com.kuaishou.invoker.Caller.AsyncFunction;
import com.kuaishou.invoker.Caller.CallDesc;
import com.kuaishou.invoker.Monitor.MonitorFactor;
import com.kuaishou.invoker.async.LazySupplier;
import com.kuaishou.invoker.downgrade.DowngradeHandler;
import com.kuaishou.invoker.downgrade.ExceptionDowngradeInfo;
import com.kuaishou.invoker.handler.ExResultHandler;
import com.kuaishou.invoker.handler.ExceptionHandler;
import com.kuaishou.invoker.handler.ResultHandler;
import com.kuaishou.invoker.manager.InvokerCallManager;
import com.kuaishou.invoker.model.ResultErrorInfo;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-01-28
 */
public class Invoker<I, O, T> {
    CallDesc desc;
    Monitor<I, O, T> monitor;
    Caller<I, O, T> caller;
    Handler<I, O, T> handler;
    Downgrader<I, O, T> downgrader;
    Recorder recorder;
    Supplier<T> defaultValue;

    Invoker(Caller<I, O, T> caller) {
        this.caller = caller;
        this.handler = new Handler<>(this);
        this.downgrader = new Downgrader<>(this);
        this.monitor = Monitor.inst().build(this);
    }

    public static <IN, OU> Caller<IN, OU, OU> call(IN instance, Function<IN, OU> function, Object... reqArgs) {
        return new Caller<>(instance, function, null, null, null, reqArgs);
    }

    public static <IN, OU> Caller<IN, OU, OU> callAsync(IN instance, Function<IN, ListenableFuture<OU>> function, long timeout, TimeUnit unit, Object... reqArgs) {
        return new Caller<>(instance, null, new AsyncFunction<>(function, timeout, unit), null, null, reqArgs);
    }

    public static <OU> Caller<Object, OU, OU> callStatic(Supplier<OU> function, Object... reqArgs) {
        return new Caller<>(null, null, null, function, null, reqArgs);
    }

    public static <OU> Caller<Object, OU, OU> callStaticAsync(Supplier<ListenableFuture<OU>> function, long timeout, TimeUnit unit, Object... reqArgs) {
        return new Caller<>(null, null, null, null, new AsyncFunction<>(function, timeout, unit), reqArgs);
    }

    public static <IN, OU> Caller<IN, OU, OU> callVoid(IN instance, Consumer<IN> function, Object... reqArgs) {
        return new Caller<>(instance, in -> {
            function.accept(instance);
            return null;
        }, null, null, null, reqArgs);
    }

    public Invoker<I, O, T> monitor(MonitorFactor monitor) {
        this.monitor = monitor.build(this);
        return this;
    }

    public ExceptionHandler<I, O, T> whenException() {
        return whenExceptionFail(ex -> true);
    }

    public ExceptionHandler<I, O, T> whenExceptionFail(Predicate<Throwable> predicate) {
        ExceptionHandler<I, O, T> handler = new ExceptionHandler<>(this, predicate);
        this.handler.addHandler(handler);
        return handler;
    }

    public ResultHandler<I, O, T> whenResultFail(Predicate<O> predicate) {
        ResultHandler<I, O, T> handler = new ResultHandler<>(this, predicate);
        this.handler.addHandler(handler);
        return handler;
    }

    public ExResultHandler<I, O, T> whenExceptionOrResultFail(Predicate<O> predicate) {
        ExResultHandler<I, O, T> handler = new ExResultHandler<>(this, predicate);
        this.handler.addHandler(handler);
        return handler;
    }

    public <RE> Invoker<I, O, RE> processor(Function<O, RE> processor) {
        return Convertor.convertInvoker(this, processor);
    }

    public Invoker<I, O, T> errorParser(Function<O, ResultErrorInfo> parser) {
        this.monitor.errorParser = parser;
        return this;
    }

    public Invoker<I, O, T> exceptionParser(Function<Throwable, ResultErrorInfo> parser) {
        this.monitor.exceptionParser = parser;
        return this;
    }

    public Invoker<I, O, T> logMethod(BiConsumer<O, Throwable> logMethod) {
        this.monitor.logMethod = logMethod;
        return this;
    }

    public Invoker<I, O, T> recorder(Recorder recorder) {
        this.recorder = recorder;
        return this;
    }

    public Invoker<I, O, T> defaultValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    public DowngradeHandler<I, O, T> whenDowngrade(Supplier<Boolean> condition) {
        DowngradeHandler<I, O, T> handler = new DowngradeHandler<I, O, T>(this, condition) {
            @Override
            public void record() {
                if (recorder != null && recorder.downgrade) {
                    InvokerCallManager.addDowngradeInfo(new ExceptionDowngradeInfo(getRealScene(), desc.getName(), "条件自动降级"));
                }
            }
        };
        this.downgrader.addHandler(handler);
        return handler;
    }


    public T getReturnValue() {
        return caller.invoke();
    }

    public void getReturnValueVoid() {
        caller.invokeVoid();
    }

    public T getReturnValueStatic() {
        return caller.invokeStatic();
    }

    public LazySupplier<T> getReturnValueAsync() {
        return caller.invokeAsync();
    }

    public LazySupplier<T> getReturnValueStaticAsync() {
        return caller.invokeStaticAsync();
    }

    public static List<ExceptionDowngradeInfo> getDowngradeInfoList() {
        return InvokerCallManager.getDowngradeInfoList();
    }

    String getRealScene() {
        Caller.CallDesc desc = this.desc;
        return StringUtils.isNotBlank(desc.getScene()) ? desc.getScene() : InvokerCallManager.getScene();
    }
}
