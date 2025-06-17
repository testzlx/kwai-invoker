package com.kuaishou.invoker;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.kuaishou.invoker.model.InvokerCallException;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-20
 */
public class Convertor {

    public static <RE, I, O, T> Invoker<I, O, RE> convertInvoker(Invoker<I, O, T> rawInvoker, Function<O, RE> processor) {
        Caller<I, O, T> rawCaller = rawInvoker.caller;
        Caller<I, O, RE> newCaller = convertCaller(rawCaller, processor);
        Invoker<I, O, RE> newInvoker = newCaller.invoker;


        if (rawInvoker.handler != null && CollectionUtils.isNotEmpty(rawInvoker.handler.handlers)) {
            throw new InvokerCallException("使用了Invoker.processor方法，禁止在processor之前声明异常处理分支！");
        }
        if (rawInvoker.downgrader != null && CollectionUtils.isNotEmpty(rawInvoker.downgrader.handlers)) {
            throw new InvokerCallException("使用了Invoker.processor方法，禁止在processor之前声明降级处理器！");
        }
        if (rawInvoker.defaultValue != null) {
            throw new InvokerCallException("使用了Invoker.processor方法，禁止在processor之前声明defaultValue！");
        }

        if (rawInvoker.monitor != null) {
            Monitor<I, O, RE> newMonitor = new Monitor<>(newInvoker, rawInvoker.monitor.factor);
            newMonitor.stopWatch = rawInvoker.monitor.stopWatch;
            newInvoker.monitor = newMonitor;
        }

        if (rawInvoker.recorder != null) {
            newInvoker.recorder = rawInvoker.recorder;
        }

        if (rawInvoker.desc != null) {
            newInvoker.desc = rawInvoker.desc;
        }
        return newInvoker;
    }

    private static <RE, I, O, T> Caller<I, O, RE> convertCaller(Caller<I, O, T> rawCaller, Function<O, RE> processor) {
        return new Caller<>(rawCaller.instance, rawCaller.function, rawCaller.asyncFunction, rawCaller.staticFunction,
                rawCaller.asyncStaticFunction, rawCaller.reqArgs, processor, rawCaller.callMode);
    }

    public static <RE, I, O, T> ListInvoker<I, O, RE> convertInvokerList(ListInvoker<I, O, T> rawInvoker, Function<O, RE> processor) {
        ListCaller<I, O, T> rawCaller = rawInvoker.caller;
        List<Caller<I, O, RE>> newCalls = rawCaller.callers.stream().map(c -> {
            Invoker<I, O, RE> ni = convertInvoker(c.invoker, processor);
            return ni.caller;
        }).collect(Collectors.toList());

        ListCaller<I, O, RE> newCaller = new ListCaller<>(newCalls);
        return new ListInvoker<>(newCaller);
    }
}
