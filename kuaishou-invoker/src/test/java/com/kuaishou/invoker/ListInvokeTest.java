package com.kuaishou.invoker;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.ListenableFuture;
import com.kuaishou.invoker.async.LazySupplier;
import com.kuaishou.invoker.downgrade.ExceptionDowngradeInfo;
import com.kuaishou.invoker.manager.InvokerCallManager;
import com.kuaishou.invoker.model.CallType;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class ListInvokeTest {
    static class TestClass {
        String calc(int arg) {
            if (arg == 2)
                throw new RuntimeException("异常");
            return String.valueOf(arg * 2);
        }

        ListenableFuture<String> asyncCalc(int arg) {
            throw new RuntimeException("test"); // 可拓展实现异步
        }
    }

    @Before
    public void before() {
        InvokerCallManager.start("单元测试");
    }

    @After
    public void end() {
        InvokerCallManager.end();
    }

    @Test
    public void testListInvokeSync() {
        TestClass instance = new TestClass();
        List<Integer> reqs = Arrays.asList(1, 5, 3);
        ListCaller<TestClass, String, String> caller = ListInvoker.callList(instance, TestClass::calc, reqs);
        ListInvoker<TestClass, String, String> invoker = caller.desc(CallType.GRPC, "同步调用");
        List<String> result = invoker.getReturnValue();
        System.out.println(result);
    }

    @Test
    public void testListProcessor() {
        TestClass instance = new TestClass();
        List<Integer> reqs = Arrays.asList(1, 5, 3);
        ListCaller<TestClass, String, String> caller = ListInvoker.callList(instance, TestClass::calc, reqs);
        ListInvoker<TestClass, String, String> invoker = caller.desc(CallType.GRPC, "处理器");
        List<String> result = invoker.processor(res -> res + "_x").getReturnValue();
        System.out.println(result);
    }

    @Test
    public void testListException() {
        TestClass instance = new TestClass();
        List<Integer> reqs = Arrays.asList(1, 2, 3);
        ListCaller<TestClass, String, String> caller = ListInvoker.callList(instance, TestClass::calc, reqs);
        ListInvoker<TestClass, String, String> invoker = caller.desc(CallType.GRPC, "异常处理");
        List<String> result = invoker.whenException().thenSkip().getReturnValue();
        System.out.println(result);
    }

    @Test
    public void testListDefaultValue() {
        TestClass instance = new TestClass();
        List<Integer> reqs = Arrays.asList(1, 2, 3);
        ListCaller<TestClass, String, String> caller = ListInvoker.callList(instance, TestClass::calc, reqs);
        ListInvoker<TestClass, String, String> invoker = caller.desc(CallType.GRPC, "默认值");
        List<String> result = invoker.defaultValue(() -> "DEF").whenException().thenSkip().getReturnValue();
        System.out.println(result);
    }

    @Test
    public void testListResultFail() {
        TestClass instance = new TestClass();
        List<Integer> reqs = Arrays.asList(1, 2, 3);
        ListCaller<TestClass, String, String> caller = ListInvoker.callList(instance, TestClass::calc, reqs);
        ListInvoker<TestClass, String, String> invoker = caller.desc(CallType.GRPC, "返回值失败");
        List<String> result = invoker.whenResultFail(res -> Objects.equal("4", res)).thenSkip().getReturnValue();
        System.out.println(result);
    }

    @Test
    public void testListAsyncInvoke() {
        TestClass instance = new TestClass();
        List<Integer> reqs = Arrays.asList(1, 2, 3);
        ListCaller<TestClass, String, String> caller = ListInvoker.callAsyncList(instance, TestClass::asyncCalc, reqs,
                1000, TimeUnit.MILLISECONDS);
        ListInvoker<TestClass, String, String> invoker = caller.desc(CallType.GRPC, "异步调用");
        LazySupplier<List<String>> asyncResult = invoker.whenException().thenReturn(res -> "ASYNC_DEF")
                .getReturnValueAsync();
        System.out.println(asyncResult.get());
    }

    @Test
    public void testListCustomReduce() {
        TestClass instance = new TestClass();
        List<Integer> reqs = Arrays.asList(1, 2, 3);
        ListCaller<TestClass, String, String> caller = ListInvoker.callList(instance, TestClass::calc, reqs);
        ListInvoker<TestClass, String, Integer> invoker = caller.desc(CallType.GRPC, "自定义归约")
                .processor(Integer::parseInt);
        Integer resultSum = invoker.getReturnValue(list -> list.stream().reduce(0, Integer::sum));
        System.out.println(resultSum);
    }

    @Test
    public void testListDowngradeInfo() {
        List<ExceptionDowngradeInfo> downgradeInfoList = ListInvoker.getDowngradeInfoList();
        System.out.println(downgradeInfoList);
    }
}
