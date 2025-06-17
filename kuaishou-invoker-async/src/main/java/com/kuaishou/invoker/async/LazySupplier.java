package com.kuaishou.invoker.async;

import static com.kuaishou.invoker.async.BusinessFlowManagerUtils.getCurrentExecutionUnit;
import static com.kuaishou.invoker.async.BusinessFlowManagerUtils.setCurrentExecutionUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2022-01-18
 * 异步化调用包装器
 */
@Slf4j
public class LazySupplier<T> implements Supplier<T> {

    /**
     * 定义获取返回值的方法
     */
    private final Supplier<? extends T> valueSupplier;
    /**
     * 定义取消本次调用的方法
     */
    @Getter
    private final Supplier<Boolean> cancelSupplier;
    /**
     * 定义返回之后的附加操作
     */
    private final List<Consumer<? super T>> attachList = new ArrayList<>();
    /**
     * 返回值缓存
     */
    private T value;

    /**
     * 是否已经初始化
     */
    private boolean initialized;

    /**
     * 是否已经取消
     */
    private boolean canceled;

    /**
     * 是否抛出了异常
     */
    private boolean exceptional;

    /**
     * 调用提示信息
     */
    private final String message;

    /**
     * 调用提示信息
     */
    private String initExecutionUnit;

    private LazySupplier(Supplier<? extends T> valueSupplier) {
        this(valueSupplier, null);
    }

    private LazySupplier(Supplier<? extends T> valueSupplier, Supplier<Boolean> cancelSupplier) {
        this(valueSupplier, cancelSupplier, null);
    }

    private LazySupplier(Supplier<? extends T> valueSupplier, Supplier<Boolean> cancelSupplier, String message) {
        this(valueSupplier, cancelSupplier, message, null);
    }

    private LazySupplier(Supplier<? extends T> valueSupplier, Supplier<Boolean> cancelSupplier, String message, String initExecutionUnit) {
        this.valueSupplier = valueSupplier;
        this.cancelSupplier = cancelSupplier;
        this.message = message;
        this.initExecutionUnit = initExecutionUnit;
        if (cancelSupplier != null) {
            AsyncRpcCallManager.add(this);
        }
    }

    public static <T> LazySupplier<T> of(T value) {
        return new LazySupplier<>(() -> value);
    }

    public static <T> LazySupplier<T> of(Supplier<? extends T> valueSupplier) {
        return new LazySupplier<>(valueSupplier);
    }

    public static <T> LazySupplier<T> of(T value, String initExecutionUnit) {
        return new LazySupplier<>(() -> value, null, null, initExecutionUnit);
    }

    public static <T> LazySupplier<T> of(Supplier<? extends T> valueSupplier, String initExecutionUnit) {
        return new LazySupplier<>(valueSupplier, null, null, initExecutionUnit);
    }

    public static <T> LazySupplier<T> of(Supplier<? extends T> valueSupplier, String message, String initExecutionUnit) {
        return new LazySupplier<>(valueSupplier, null, message, initExecutionUnit);
    }

    public static <T> LazySupplier<T> of(Supplier<? extends T> valueSupplier, Supplier<Boolean> cancelSupplier) {
        return new LazySupplier<>(valueSupplier, cancelSupplier);
    }

    public static <T> LazySupplier<T> of(Supplier<? extends T> valueSupplier, Supplier<Boolean> cancelSupplier, String message,
            String initExecutionUnit) {
        return new LazySupplier<>(valueSupplier, cancelSupplier, message, initExecutionUnit);
    }

    /**
     * 获取返回值，非线程安全
     */
    @Override
    public T get() {
        if (!initialized) {
            if (valueSupplier == null) {
                initialized = true;
                return value;
            }
            String currentExecutionUnit = null;
            if (StringUtils.isNotBlank(initExecutionUnit)) {
                currentExecutionUnit = getCurrentExecutionUnit();
                setCurrentExecutionUnit(initExecutionUnit);
            }
            try {
                this.value = valueSupplier.get();
                for (Consumer<? super T> consumer : attachList) {
                    consumer.accept(value);
                }
                initialized = true;
                if (StringUtils.isNotBlank(currentExecutionUnit)) {
                    setCurrentExecutionUnit(currentExecutionUnit);
                }
            } catch (Throwable t) {
                exceptional = true;
                // 能抛出异常的都是强依赖，强依赖异常取消其他调用
                AsyncRpcCallManager.cancel();
                // 调用失败抛出异常，给上游通知
                throw t;
            }
        }
        return value;
    }

    /**
     * 取消当前调用，非线程安全
     */
    public boolean cancel() {
        boolean res = true;
        if (!canceled) {
            if (cancelSupplier == null) {
                canceled = true;
                log.error(String.format("%s cancel function not exist", this));
                return false;
            }
            try {
                res = cancelSupplier.get();
            } catch (Throwable t) {
                log.error(String.format("%s cancel exception", this), t);
                // 取消失败吞掉异常，不影响后面的取消
            }
        }
        canceled = true;
        return res;
    }

    /**
     * 是否已经结束，正常返回、抛出异常、被取消均为已结束
     */
    public boolean isFinish() {
        return initialized || exceptional || canceled;
    }

    public void attach(Consumer<? super T> consumer) {
        this.attachList.add(consumer);
    }

    public <S> LazySupplier<S> map(Function<? super T, ? extends S> function) {
        return LazySupplier.of(() -> function.apply(get()), String.format("%s-map", this.message), this.initExecutionUnit);
    }

    public <S> LazySupplier<S> flatMap(Function<? super T, LazySupplier<? extends S>> function) {
        return LazySupplier.of(() -> function.apply(get()).get(), String.format("%s-flatMap", this.message), this.initExecutionUnit);
    }

    @Override
    public String toString() {
        return "【LazySupplier：" + message + "】";
    }
}
