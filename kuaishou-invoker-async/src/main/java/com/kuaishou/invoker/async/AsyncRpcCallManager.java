package com.kuaishou.invoker.async;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2022-02-07
 * 异步调用管理器
 */
@Slf4j
public class AsyncRpcCallManager {

    private static final ThreadLocal<LazySupplierContext> ASYNC_LAZY_CONTAINER = ThreadLocal.withInitial(LazySupplierContext::new);

    /**
     * 开启一连串异步调用
     */
    public static void start(boolean withExecutionUnit) {
        ASYNC_LAZY_CONTAINER.set(new LazySupplierContext(true, withExecutionUnit));
    }

    /**
     * 结束一连串异步调用
     */
    public static void end() {
        try {
            List<LazySupplier<?>> lazyList = ASYNC_LAZY_CONTAINER.get().getLazyList();
            if (CollectionUtils.isNotEmpty(lazyList)) {
                for (LazySupplier<?> lazy : lazyList) {
                    if (!lazy.isFinish()) {
                        lazy.get();
                    }
                }
            }
        } finally {
            ASYNC_LAZY_CONTAINER.remove();
        }
    }

    /**
     * 添加一个Lazy对象进入线程上下文
     */
    public static void add(LazySupplier<?> lazy) {
        LazySupplierContext context = ASYNC_LAZY_CONTAINER.get();
        //只有显示开启了上下文的需要add进来
        if (context != null && context.isStarted()) {
            context.getLazyList().add(lazy);
        }
    }

    /**
     * 取消一连串异步调用
     */
    public static void cancel() {
        try {
            List<LazySupplier<?>> lazyList = ASYNC_LAZY_CONTAINER.get().getLazyList();
            if (CollectionUtils.isNotEmpty(lazyList)) {
                for (LazySupplier<?> lazy : lazyList) {
                    if (lazy.getCancelSupplier() != null && !lazy.isFinish()) {
                        lazy.cancel();
                    }
                }
            }
        } finally {
            ASYNC_LAZY_CONTAINER.remove();
        }
    }

    public static LazySupplierContext getLazyContext() {
        return ASYNC_LAZY_CONTAINER.get();
    }

    public static void remove() {
        ASYNC_LAZY_CONTAINER.remove();
    }

    @Data
    public static class LazySupplierContext {
        private List<LazySupplier<?>> lazyList = new ArrayList<>();
        private boolean started = false;
        private boolean withExecutionUnit = false;

        public LazySupplierContext() {
        }

        public LazySupplierContext(boolean started) {
            this.started = started;
        }

        public LazySupplierContext(boolean started, boolean withExecutionUnit) {
            this.started = started;
            this.withExecutionUnit = withExecutionUnit;
        }
    }

    public static boolean withExecutionUnit() {
        LazySupplierContext context = getLazyContext();
        if (context == null) {
            return false;
        }
        return context.isWithExecutionUnit();
    }
}
