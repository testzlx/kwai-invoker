package com.kuaishou.invoker.async;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static com.kuaishou.invoker.async.BusinessFlowManagerUtils.getCurrentExecutionUnit;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2022-02-08
 * 代表一次GRPC调用
 */
public class GrpcCall<T, S> {

    /**
     * 定义同步调用方法
     */
    private final Supplier<T> syncSupplier;
    /**
     * 定义异步调用方法
     */
    private final ThrowableSupplier<T> asyncSupplier;
    /**
     * 定义取消的方法
     */
    private final Supplier<Boolean> cancelSupplier;
    /**
     * 定义异步future的获取方法
     */
    private final FutureConfig<T> futureConfig;
    /**
     * 定义调用返回后的处理方法
     */
    private final SupplierProcessor<T, S> supplierProcessor;

    /**
     * 本次异步化调用的名称
     */
    private final String name;

    /**
     * 是否在执行单元中
     */
    private final boolean withExecutionUnit;

    public GrpcCall(Supplier<T> syncSupplier, ThrowableSupplier<T> asyncSupplier, Supplier<Boolean> cancelSupplier,
                    FutureConfig<T> futureConfig, SupplierProcessor<T, S> supplierProcessor, String name, boolean withExecutionUnit) {
        this.syncSupplier = syncSupplier;
        this.asyncSupplier = asyncSupplier;
        this.cancelSupplier = cancelSupplier;
        this.futureConfig = futureConfig;
        this.supplierProcessor = supplierProcessor;
        this.name = name;
        this.withExecutionUnit = withExecutionUnit;
    }

    public static <T, S> GrpcCallBuilder<T, S> newBuilder() {
        return new GrpcCallBuilder<>();
    }

    /**
     * 触发GRPC调用
     */
    public LazySupplier<S> call(boolean async) {
        String initExecutionUnit = null;
        if (withExecutionUnit) {
            initExecutionUnit = getCurrentExecutionUnit();
        }
        Preconditions.checkArgument(supplierProcessor != null, "GrpcCall processor is null");
        // 同步
        if (!async) {
            Preconditions.checkArgument(syncSupplier != null, "GrpcCall sync method is null");
            LazySupplier<S> syncLazy =
                    LazySupplier.of(() -> supplierProcessor.process(ThrowableSupplier.warp(syncSupplier)), name, initExecutionUnit);
            // 同步需要立刻触发计算
            syncLazy.get();
            return syncLazy;
        }
        Preconditions.checkArgument(asyncSupplier != null || futureConfig != null, "GrpcCall async method is null");
        // 异步future模式
        if (futureConfig != null) {
            // 发起异步调用
            ListenableFuture<T> future;
            try {
                future = futureConfig.futureSupplier.get();
            } catch (Throwable t) {
                ListenableFutureTask<T> futureTask = ListenableFutureTask.create(() -> {
                    throw t;
                });
                futureTask.run();
                future = futureTask;
            }
            // 设置超时时间
            futureConfig.calcSetEndNanos();
            // 返还异步结果
            ListenableFuture<T> finalFuture = future;
            return LazySupplier.of(
                    () -> supplierProcessor.process(() -> finalFuture.get(futureConfig.currentTimeoutNanos(), TimeUnit.NANOSECONDS)),
                    () -> finalFuture.cancel(true),
                    name, initExecutionUnit
            );
        }
        // 异步supplier模式
        return LazySupplier.of(() -> supplierProcessor.process(asyncSupplier), cancelSupplier, name, initExecutionUnit);
    }

    public LazySupplier<S> asyncCall() {
        return call(true);
    }

    public LazySupplier<S> syncCall() {
        return call(false);
    }

    public static class GrpcCallBuilder<T, S> {

        private Supplier<T> syncSupplier;
        private ThrowableSupplier<T> asyncSupplier;
        private Supplier<Boolean> cancelSupplier;
        private FutureConfig<T> futureConfig;
        private SupplierProcessor<T, S> processor;
        private String name;
        private boolean withExecutionUnit;

        public GrpcCallBuilder<T, S> sync(Supplier<T> supplier) {
            this.syncSupplier = supplier;
            return this;
        }

        public GrpcCallBuilder<T, S> async(ThrowableSupplier<T> supplier) {
            this.asyncSupplier = supplier;
            return this;
        }

        public GrpcCallBuilder<T, S> async(ThrowableCancelSupplier<T> supplier) {
            this.asyncSupplier = supplier;
            this.cancelSupplier = supplier::cancel;
            return this;
        }

        public GrpcCallBuilder<T, S> async(Supplier<ListenableFuture<T>> futureSupplier, long timeout, TimeUnit unit) {
            this.futureConfig = new FutureConfig<T>(futureSupplier, unit.toNanos(timeout));
            return this;
        }

        public GrpcCallBuilder<T, S> processor(SupplierProcessor<T, S> processor) {
            this.processor = processor;
            return this;
        }

        public GrpcCallBuilder<T, S> listener(Runnable listener) {
            Supplier<ListenableFuture<T>> futureSupplier = futureConfig.futureSupplier;
            this.futureConfig = new FutureConfig<>(() -> {
                ListenableFuture<T> future = futureSupplier.get();
                future.addListener(listener, directExecutor());
                return future;
            }, futureConfig.timeoutNanos);
            return this;
        }

        public GrpcCallBuilder<T, S> name(String name) {
            this.name = name;
            return this;
        }

        public GrpcCallBuilder<T, S> withExecutionUnit(boolean withExecutionUnit) {
            this.withExecutionUnit = withExecutionUnit;
            return this;
        }

        public GrpcCall<T, S> build() {
            return new GrpcCall<>(syncSupplier, asyncSupplier, cancelSupplier, futureConfig, processor, name, withExecutionUnit);
        }
    }

    @FunctionalInterface
    public interface ThrowableSupplier<T> {
        T get() throws Exception;

        static <T> ThrowableSupplier<T> warp(Supplier<T> supplier) {
            return supplier::get;
        }
    }

    public interface ThrowableCancelSupplier<T> extends ThrowableSupplier<T> {
        @Override
        T get() throws Exception;

        Boolean cancel();
    }

    @FunctionalInterface
    public interface SupplierProcessor<T, S> {
        S process(ThrowableSupplier<T> supplier);
    }


    private static class FutureConfig<T> {

        private final Supplier<ListenableFuture<T>> futureSupplier;
        private final long timeoutNanos;
        private long futureEndNanos;

        public FutureConfig(Supplier<ListenableFuture<T>> futureSupplier, long timeoutNanos) {
            this.futureSupplier = futureSupplier;
            this.timeoutNanos = timeoutNanos;
        }

        void calcSetEndNanos() {
            this.futureEndNanos = System.nanoTime() + timeoutNanos;
        }

        long currentTimeoutNanos() {
            return futureEndNanos - System.nanoTime();
        }
    }

    public static ChronoUnit toChronoUnit(TimeUnit timeUnit) {
        switch (timeUnit) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                throw new AssertionError();
        }
    }
}
