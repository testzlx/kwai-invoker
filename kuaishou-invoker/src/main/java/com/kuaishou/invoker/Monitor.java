package com.kuaishou.invoker;


import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;

import com.ecyrd.speed4j.StopWatch;
import com.ecyrd.speed4j.StopWatchFactory;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.kuaishou.invoker.util.Json.JsonUtil;
import com.kuaishou.invoker.model.HandleInfo;
import com.kuaishou.invoker.model.ResultErrorInfo;
import com.kuaishou.invoker.model.StrongType;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-11
 */
@Slf4j
public class Monitor<I, O, T> {

    public static final Supplier<StopWatchFactory> stopWatchFactory = StopWatchFactory::getDefault;

    final Invoker<I, O, T> invoker;
    final MonitorFactor factor;
    StopWatch stopWatch;
    Function<O, ResultErrorInfo> errorParser;
    Function<Throwable, ResultErrorInfo> exceptionParser;
    BiConsumer<O, Throwable> logMethod;

    Monitor(Invoker<I, O, T> invoker, MonitorFactor factor) {
        this.invoker = invoker;
        this.factor = factor;
    }

    public static MonitorFactor inst() {
        return new MonitorFactor();
    }

    public void monitorReq() {
        if (factor.perfType == PerfType.ALL) {
            stopWatch = stopWatchFactory.get().getStopWatch();
        }
    }

    public void monitorResult(Throwable t, O res, HandleInfo handleInfo) {
        Caller.CallDesc desc = invoker.desc;
        LogFactor logFactor = factor.logFactor;
        String callMode = invoker.caller.callMode.name();
        String scene = invoker.getRealScene();

        if (t != null) {
            if (logFactor.logException()) {
                if (logMethod != null) {
                    logMethod.accept(res, t);
                } else {
                    String logStr = String.format("%s-%s-%s-%s-exception", scene, desc.getName(), desc.getType(), callMode);
                    if (logFactor.logRequest()) {
                        logStr += String.format(", request: %s", getRequestStr());
                    }
                    if (handleInfo.getReturnDefaultReason() != null) {
                        logStr += String.format(", returnDefaultValue: %s", handleInfo.getReturnDefaultReason());
                    }
                    if (logFactor.withException) {
                        log.error(logStr, t);
                    } else {
                        log.error(logStr);
                    }
                }
            }
            ResultErrorInfo errorInfo = parseException(t);
            doPerf(String.format("exception:%s:%s", errorInfo.getErrorCode(), errorInfo.getErrorMsg()), scene);
            return;
        }

        if (logFactor.logFinish()) {
            if (logMethod != null) {
                logMethod.accept(res, null);
            } else if (handleInfo.isHasFail() || logFactor.logSuccess()) {
                //如果失败则打印日志，如果允许打印成功日志再打印
                String logStr =
                        String.format("%s-%s-%s-%s-finish", scene, desc.getName(), desc.getType(), callMode);
                if (logFactor.logRequest()) {
                    logStr += String.format(", request: %s", getRequestStr());
                }
                if (logFactor.logResponse()) {
                    logStr += String.format(", response: %s", JsonUtil.of(res));
                }
                if (handleInfo.getReturnDefaultReason() != null) {
                    logStr += String.format(", returnDefaultValue: %s", handleInfo.getReturnDefaultReason());
                }
                log.info(logStr);

            }
        }
        String msg = "success";
        if (handleInfo.isHasFail()) {
            msg = "fail";
            if (errorParser != null) {
                ResultErrorInfo errorInfo = errorParser.apply(res);
                if (errorInfo != null) {
                    msg = String.format("%s:%s:%s", "fail", errorInfo.getErrorCode(), errorInfo.getErrorMsg());
                }
            }
        }
        doPerf(msg, scene);
    }

    private void doPerf(String result, String domain) {
        Caller.CallDesc desc = invoker.desc;
        PerfType perfType = factor.perfType;
        String asyncMode = invoker.caller.callMode.name();
        StrongType strongType = invoker.handler.getStrongType();
        String downgrade = CollectionUtils.isEmpty(invoker.downgrader.handlers) ? "0" : "1";
        if (strongType == StrongType.WEAK && CollectionUtils.isEmpty(invoker.downgrader.handlers)) {
//            secPerf(InvokerCallRegister.getPerfDomain(), domain, desc.getName(),
//                    desc.getType().name(), "weak but no downgrade");
        }
        if (perfType != PerfType.NONE) {
            if (stopWatch != null) {
//                secPerf(InvokerCallRegister.getPerfDomain(), domain, stopWatch, desc.getName(),
//                        desc.getType().name(), strongType.name(), downgrade, asyncMode, result);
            } else {
//                secPerf(InvokerCallRegister.getPerfDomain(), domain, desc.getName(),
//                        desc.getType().name(), strongType.name(), downgrade, asyncMode, result);
            }
        }
    }

    private String getRequestStr() {
        Object[] reqArgs = invoker.caller.reqArgs;
        if (reqArgs == null || reqArgs.length == 0) {
            return "not_set_reqArgs";
        }
        if (reqArgs.length == 1) {
            return JsonUtil.of(reqArgs[0]);
        }
        return JsonUtil.of(Arrays.asList(reqArgs));
    }


    public static class MonitorFactor {
        private PerfType perfType = PerfType.ALL;
        private final LogFactor logFactor = new LogFactor();

        public MonitorFactor disablePerf() {
            this.perfType = PerfType.NONE;
            return this;
        }

        public MonitorFactor disableLog() {
            this.logFactor.disableLog = true;
            return this;
        }

        public MonitorFactor enableStructLog(Supplier<Boolean> rateCondition) {
            this.logFactor.enableStructLog = rateCondition;
            return this;
        }

        public MonitorFactor logRequest(@NonNull Supplier<Boolean> rateCondition) {
            this.logFactor.withRequest = rateCondition;
            return this;
        }

        public MonitorFactor logResponse(@NonNull Supplier<Boolean> rateCondition) {
            this.logFactor.withResponse = rateCondition;
            return this;
        }

        /**
         * 不打印请求
         */
        public MonitorFactor notLogRequest() {
            this.logFactor.withRequest = () -> false;
            return this;
        }

        /**
         * 不打印响应
         */
        public MonitorFactor notLogResponse() {
            this.logFactor.withResponse = () -> false;
            return this;
        }

        /**
         * 不打印异常堆栈
         */
        public MonitorFactor notLogException() {
            this.logFactor.withException = false;
            return this;
        }

        /**
         * 所有日志的打印条件（调用成功、业务自定义失败、异常日志）
         */
        public MonitorFactor logCondition(Supplier<Boolean> rateCondition) {
            this.logFactor.rateCondition = rateCondition;
            return this;
        }

        /**
         * 完成日志的打印条件（调用成功、业务自定义失败日志）
         */
        public MonitorFactor logFinishCondition(Supplier<Boolean> rateCondition) {
            this.logFactor.finishCondition = rateCondition;
            return this;
        }

        /**
         * 异常日志打印条件
         */
        public MonitorFactor logExceptionCondition(Supplier<Boolean> rateCondition) {
            this.logFactor.exceptionCondition = rateCondition;
            return this;
        }

        /**
         * 成功日志打印条件
         */
        public MonitorFactor logSuccessCondition(Supplier<Boolean> rateCondition) {
            this.logFactor.successCondition = rateCondition;
            return this;
        }

        public MonitorFactor perfType(PerfType perfType) {
            this.perfType = perfType;
            return this;
        }

        public <I, O, T> Monitor<I, O, T> build(Invoker<I, O, T> invoker) {
            Monitor<I, O, T> newMonitor = new Monitor<>(invoker, this);
            Monitor<I, O, T> rawMonitor = invoker.monitor;
            if (rawMonitor != null) {
                newMonitor.logMethod = rawMonitor.logMethod;
                newMonitor.errorParser = rawMonitor.errorParser;
                newMonitor.exceptionParser = rawMonitor.exceptionParser;
            }
            return newMonitor;
        }
    }

    public static class LogFactor {
        private Supplier<Boolean> withRequest = () -> true;
        private Supplier<Boolean> withResponse = () -> true;
        private boolean withException = true;
        private boolean disableLog = false;
        private Supplier<Boolean> enableStructLog = () -> false;
        private Supplier<Boolean> rateCondition = () -> true;
        private Supplier<Boolean> finishCondition = () -> true;
        private Supplier<Boolean> exceptionCondition = () -> true;
        private Supplier<Boolean> successCondition = () -> true;

        boolean logFinish() {
            return !disableLog && rateCondition.get() && finishCondition.get();
        }

        boolean logException() {
            return !disableLog && rateCondition.get() && exceptionCondition.get();
        }

        boolean logSuccess() {
            return logFinish() && successCondition.get();
        }

        boolean logRequest() {
            return withRequest.get();
        }

        boolean logResponse() {
            return withResponse.get();
        }
    }

    public enum PerfType {
        NONE,
        ALL,
        ONLY_FLOW;
    }

    public ResultErrorInfo parseException(Throwable throwable) {
        if (throwable instanceof ExecutionException && throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        if (throwable instanceof TimeoutException || throwable instanceof UncheckedTimeoutException) {
            return new ResultErrorInfo("101", "调用超时");
        }

        if (exceptionParser != null) {
            ResultErrorInfo errorInfo = exceptionParser.apply(throwable);
            if (errorInfo != null) {
                return errorInfo;
            }
        }
        return new ResultErrorInfo(throwable.getClass().getSimpleName(), String.format("未定义异常:%s", throwable.getMessage()));
    }
}
