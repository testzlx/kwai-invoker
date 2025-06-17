package com.kuaishou.invoker.concurrent;

import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author liuzhuo
 * Created on 2023-04-03 下午2:04
 * Invoker线程池包装
 * {@link InvokerCallable}
 * {@link InvokerRunnable}
 */
public class InvokerExecutors {

    public static ExecutorService getInvokerExecutorService(ExecutorService executorService) {
        if (Objects.isNull(executorService)) {
            return null;
        }
        return new ExecutorServiceInvokerWrapper(executorService);
    }

    public static Executor getInvokerExecutor(Executor executor) {
        if (Objects.isNull(executor)) {
            return null;
        }
        return new ExecutorInvokerWrapper(executor);
    }

    public static ListeningExecutorService getInvokerListeningExecutorService(ListeningExecutorService listeningExecutorService) {
        if (Objects.isNull(listeningExecutorService)) {
            return null;
        }
        return new ListeningExecutorServiceInvokerWrapper(listeningExecutorService);
    }
}
