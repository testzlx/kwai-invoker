package com.kuaishou.invoker.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author liuzhuo
 * Created on 2023-04-04 下午12:14
 * ListeningExecutorService包装
 *  {@link InvokerCallable}
 *  {@link InvokerRunnable}
 */
public class ListeningExecutorServiceInvokerWrapper implements ListeningExecutorService {

    private final ListeningExecutorService listeningExecutorService;

    public ListeningExecutorServiceInvokerWrapper(
            ListeningExecutorService listeningExecutorService) {
        this.listeningExecutorService = listeningExecutorService;
    }

    @Override
    public void shutdown() {
        listeningExecutorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return listeningExecutorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return listeningExecutorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return listeningExecutorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return listeningExecutorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> ListenableFuture<T> submit(Callable<T> task) {
        return listeningExecutorService.submit(InvokerCallable.wrap(task));
    }

    @Override
    public ListenableFuture<?> submit(Runnable task) {
        return listeningExecutorService.submit(InvokerRunnable.wrap(task));
    }

    @Override
    public <T> ListenableFuture<T> submit(Runnable task, T result) {
        return listeningExecutorService.submit(InvokerRunnable.wrap(task), result);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return listeningExecutorService.invokeAll(InvokerCallable.wraps(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return listeningExecutorService.invokeAll(InvokerCallable.wraps(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return listeningExecutorService.invokeAny(InvokerCallable.wraps(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return listeningExecutorService.invokeAny(InvokerCallable.wraps(tasks), timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        listeningExecutorService.execute(InvokerRunnable.wrap(command));
    }
}
