package com.kuaishou.invoker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;

import com.kuaishou.invoker.async.LazySupplier;
import com.kuaishou.invoker.model.CallType;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-26
 */
public class ListCaller<I, O, T> {

    final List<Caller<I, O, T>> callers;
    final ListInvoker<I, O, T> invoker;

    ListCaller(List<Caller<I, O, T>> callers) {
        this.callers = ListUtils.emptyIfNull(callers);
        this.invoker = new ListInvoker<>(this);
    }

    public ListInvoker<I, O, T> desc(CallType type, String name) {
        callers.forEach(c -> c.desc(type, name));
        return invoker;
    }


    public ListInvoker<I, O, T> desc(CallType type, String name, String domain) {
        callers.forEach(c -> c.desc(type, name, domain));
        return invoker;
    }

    <RES> RES invoke(Function<List<T>, RES> reducer) {
        List<T> list = callers.stream().map(Caller::invoke).collect(Collectors.toList());
        return reducer.apply(list);
    }

    <RES> LazySupplier<RES> invokeAsync(Function<List<T>, RES> reducer) {
        LazySupplier<List<T>> finalRes = LazySupplier.of(new ArrayList<>());
        for (Caller<I, O, T> caller : callers) {
            LazySupplier<T> subRes = caller.invokeAsync();
            // 发起级联调用
            finalRes = finalRes.map(list -> {
                list.add(subRes.get());
                return list;
            });
        }
        return finalRes.map(reducer);
    }
}
