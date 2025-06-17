package com.kuaishou.invoker;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.ListenableFuture;
import com.kuaishou.invoker.async.LazySupplier;
import com.kuaishou.invoker.downgrade.ExceptionDowngradeInfo;
import com.kuaishou.invoker.manager.InvokerCallManager;
import com.kuaishou.invoker.model.CallType;
import com.kuaishou.invoker.model.ResultErrorInfo;
import com.kuaishou.invoker.model.StrongType;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-22
 */
@Slf4j
public class InvokerTest {

    static class TestClass {

        int testMethod(String arg) {
            throw new RuntimeException("异常");
        }

        String testMethod2(String arg) {
            return "1";
        }


        void testMethodVoid() {
            System.out.println("hhh");
        }

        static String testMethodStatic(int arg) {
            return "static";
        }


        void be() {
            throw new RuntimeException("exception");
        }

        String c(int arg) {
            //            return String.valueOf(arg);
            throw new RuntimeException("test");
        }

        long d(int arg1, int arg2) {
            return arg1 + arg2;
        }


        ListenableFuture<String> testMethodAsync(int arg) {
            //            ExecutorService executorService = Executors.newCachedThreadPool();
            //            ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(executorService);
            //            return listeningExecutorService.submit(() -> {
            //                System.out.println("start");
            //                Thread.sleep(1000);
            //                System.out.println("end");
            //                return "async";
            //            });
            throw new RuntimeException("test");
        }

        static ListenableFuture<String> testMethodAsyncStatic(int arg) {
//            ExecutorService executorService = Executors.newCachedThreadPool();
//            ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(executorService);
//            return listeningExecutorService.submit(() -> {
//                System.out.println("start");
//                Thread.sleep(1000);
//                System.out.println("end");
//                return "async";
//            });
            throw new RuntimeException("test");
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
    public void testSimple() {
        TestClass instance = new TestClass();
        Integer res = Invoker.call(instance, f -> f.testMethod("arg"))
                .desc(CallType.GRPC, "测试方法A")
                .getReturnValue();
        System.out.println(res);
    }

    @Test
    public void testVoid() {
        TestClass instance = new TestClass();
        Invoker.callVoid(instance, TestClass::testMethodVoid)
                .desc(CallType.GRPC, "测试方法A")
                .getReturnValueVoid();
    }

    @Test
    public void testStatic() {
        String value = Invoker.callStatic(() -> TestClass.testMethodStatic(1))
                .desc(CallType.GRPC, "测试方法A")
                .getReturnValueStatic();
        System.out.println(value);
    }

    @Test
    public void testException() {
        TestClass instance = new TestClass();
        Invoker.callVoid(instance, TestClass::be)
                .desc(CallType.GRPC, "测试方法A", StrongType.WEAK)
                .getReturnValueVoid();
    }

    @Test
    public void testExceptionFail() {
        TestClass instance = new TestClass();
        Invoker.callVoid(instance, TestClass::be)
                .desc(CallType.GRPC, "测试方法A")
                .whenException()
                .thenThrow(e -> new IllegalArgumentException("aaa"))
                .getReturnValueVoid();
    }

    @Test
    public void testResultFail() {
        TestClass instance = new TestClass();
        Integer value = Invoker.call(instance, f -> f.testMethod("arg"))
                .desc(CallType.GRPC, "测试方法A")
                .whenResultFail(res -> res == 2)
                .thenThrow(res -> new RuntimeException("111"))
                .getReturnValue();
        System.out.println(value);
    }


    @Test
    public void testResultMulti2() {
        TestClass instance = new TestClass();
        Integer value = Invoker.call(instance, f -> f.testMethod("arg"))
                .desc(CallType.GRPC, "测试方法A")
                .whenResultFail(res -> res == 1)
                .thenThrow(res -> new RuntimeException("throw"))
                .whenResultFail(res -> res >= 2)
                .thenAction(res -> System.out.println(res + "second"))
                .getReturnValue();
        System.out.println(value);
    }


    @Test
    public void testProcessor() {
        TestClass instance = new TestClass();
        String value = Invoker.call(instance, f -> f.testMethod("arg"))
                .desc(CallType.GRPC, "测试方法A")
                .processor(res -> res + "1")
                .whenResultFail(res -> res == 2)
                .thenThrow(res -> new RuntimeException("throw"))
                .whenResultFail(res -> res >= 1)
                .thenReturn(res -> "222")
                .getReturnValue();
        System.out.println(value);
    }

    @Test
    public void testAsync() {
        TestClass instance = new TestClass();
        LazySupplier<String> valueAsync = Invoker.callAsync(instance, f -> f.testMethodAsync(111), 1000, TimeUnit.MILLISECONDS, 1, 2, 3)
                .desc(CallType.GRPC, "测试方法B")
                .processor(res -> res + "1")
                .whenResultFail(res -> res.equals("11"))
                .thenThrow(res -> new RuntimeException("throw"))
                .whenResultFail(res -> res.equals("11"))
                .thenReturn(res -> "222")
                .getReturnValueAsync();

        System.out.println("hhhh");
        valueAsync = valueAsync.map(res -> res + "2");
        String value = valueAsync.get();
        System.out.println(value);
    }

    @Test
    public void testMonitor() {
        TestClass instance = new TestClass();
        Invoker.callVoid(instance, TestClass::be)
                .desc(CallType.GRPC, "测试方法A")
                .recorder(Recorder.inst().onException())
                .monitor(Monitor.inst().notLogRequest())
                .whenException()
                .thenAction(e -> System.out.println("skip"))
                .getReturnValueVoid();

        List<ExceptionDowngradeInfo> downgradeInfoList = Invoker.getDowngradeInfoList();
        System.out.println(downgradeInfoList);
    }

    @Test
    public void testDowngrade() {
        TestClass instance = new TestClass();
        String value = Invoker.call(instance, f -> f.c(1))
                .desc(CallType.GRPC, "测试方法降级")
                .whenDowngrade(() -> true)
                .thenReturn(() -> "downgrade")
                .whenDowngrade(() -> true)
                .thenReturn(() -> "trueDowngrade")
                .recorder(Recorder.inst().onException().onDowngrade())
                .monitor(Monitor.inst().notLogRequest())
                .whenException()
                .thenAction(e -> System.out.println("skip"))
                .getReturnValue();
        System.out.println(value);

        List<ExceptionDowngradeInfo> downgradeInfoList = Invoker.getDowngradeInfoList();
        System.out.println(downgradeInfoList);
    }

    @Test
    public void testIter() {
        List<Integer> reqs = Arrays.asList(1, 2, 3);
        TestClass instance = new TestClass();
        Integer resList = ListInvoker.callList(instance, TestClass::c, reqs)
                .desc(CallType.GRPC, "测试循环")
                .processor(Integer::parseInt)
                .defaultValue(() -> 123)
                .whenDowngrade(() -> true)
                .thenSkip()
                .recorder(Recorder.inst().onDowngrade())
                .whenException()
                .thenSkip()
                .whenResultFail(res -> Objects.equal("111", res))
                .thenSkip()
                .getReturnValue(res -> res.stream().mapToInt(Integer::intValue).sum());
        System.out.println(resList);
        System.out.println(ListInvoker.getDowngradeInfoList());
    }

    @Test
    public void testIterAsync() {
        List<Integer> reqs = Arrays.asList(1, 2, 3);
        TestClass instance = new TestClass();
        LazySupplier<String> valueAsync = ListInvoker.callAsyncList(instance, TestClass::testMethodAsync, reqs, 1000, TimeUnit.MILLISECONDS)
                .desc(CallType.GRPC, "测试循环")
                .processor(s -> s + "111")
                .whenException()
                .thenReturn(res -> "exception")
                .getReturnValueAsync(res -> String.join(",", res));
        System.out.println(valueAsync.get());
    }

    @Test
    public void testLogMethod() {
        TestClass instance = new TestClass();
        Integer value = Invoker.call(instance, f -> f.testMethod("arg"))
                .desc(CallType.GRPC, "测试日志方法")
                // 全面接管日志打印方法，参数为（返回值，异常），2者只会出现一种
                .logMethod((res, t) -> {
                    if (res != null) {
                        log.info("自定义日志返回:{}", res);
                    }
                    if (t != null) {
                        log.error("自定义日志异常", t);
                    }
                })
                // 指定日志生效条件，staging环境
                .monitor(Monitor.inst().logCondition(() -> true))
                .exceptionParser(e -> new ResultErrorInfo("111", "异常解析"))
                .getReturnValue();
        System.out.println(value);
    }


    @Test
    public void testProcessorCheck() {
        TestClass instance = new TestClass();
        String value = Invoker.call(instance, f -> f.testMethod("arg"))
                .desc(CallType.GRPC, "测试日志方法")
                //                                .whenDowngrade(1).thenSkip()
                .whenException().thenSkip()
                .processor(String::valueOf)
                .getReturnValue();
        System.out.println(value);
    }

    @Test
    public void testDefaultValue() {
        TestClass instance = new TestClass();
        String value = Invoker.call(instance, f -> f.testMethod("arg"))
                .desc(CallType.GRPC, "测试日志方法")
                .processor(String::valueOf)
                .whenException().thenSkip()
                .getReturnValue();
        System.out.println(value);
    }

    @Test
    public void testAsyncStatic() {
        LazySupplier<String> valueAsync = Invoker.callStaticAsync(() -> TestClass.testMethodAsyncStatic(111), 1000, TimeUnit.MILLISECONDS, 1, 2, 3)
                .desc(CallType.GRPC, "测试方法B")
                .processor(res -> res + "1")
                .whenResultFail(res -> res.equals("11"))
                .thenThrow(res -> new RuntimeException("throw"))
                .whenResultFail(res -> res.equals("11"))
                .thenReturn(res -> "222")
                .getReturnValueStaticAsync();

        System.out.println("hhhh");
        valueAsync = valueAsync.map(res -> res + "2");
        String value = valueAsync.get();
        System.out.println(value);
    }
}