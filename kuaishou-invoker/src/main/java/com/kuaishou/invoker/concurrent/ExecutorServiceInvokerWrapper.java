package com.kuaishou.invoker.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author liuzhuo
 * Created on 2023-04-03 下午1:47
 * ExecutorService包装
 *  {@link InvokerCallable}
 *  {@link InvokerRunnable}
 */
public class ExecutorServiceInvokerWrapper extends ExecutorInvokerWrapper implements ExecutorService {

    private final ExecutorService executorService;

    public ExecutorServiceInvokerWrapper(ExecutorService executorService) {
        super(executorService);
        this.executorService = executorService;
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(InvokerCallable.wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(InvokerRunnable.wrap(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(InvokerRunnable.wrap(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(InvokerCallable.wraps(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return executorService.invokeAll(InvokerCallable.wraps(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(InvokerCallable.wraps(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(InvokerCallable.wraps(tasks), timeout, unit);
    }
}
