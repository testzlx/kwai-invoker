package com.kuaishou.invoker.concurrent;

/**
 * @author liuzhuo
 * Created on 2023-03-30 下午9:17
 * runnable wrap of Invoker
 * <p>
 * {@link InvokerCallable}
 */
public final class InvokerRunnable implements Runnable {

    private final InvokerContext context;

    private final Runnable runnable;

    @Override
    public void run() {
        try {
            context.setCurrentContext();
            runnable.run();
        } finally {
            context.removeCurrentContext();
        }
    }

    private InvokerRunnable(Runnable runnable) {
        this.context = InvokerContext.getCurrentContext();
        this.runnable = runnable;
    }

    public static InvokerRunnable wrap(Runnable runnable) {
        return new InvokerRunnable(runnable);
    }

}
