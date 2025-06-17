package com.kuaishou.invoker;

import com.kuaishou.invoker.downgrade.ExceptionDowngradeInfo;
import com.kuaishou.invoker.handler.CallHandler;
import com.kuaishou.invoker.handler.ExResultHandler;
import com.kuaishou.invoker.handler.ExceptionHandler;
import com.kuaishou.invoker.handler.ResultHandler;
import com.kuaishou.invoker.manager.InvokerCallManager;
import com.kuaishou.invoker.model.HandleInfo;
import com.kuaishou.invoker.model.InvokerCallException;
import com.kuaishou.invoker.model.StrongType;
import com.kuaishou.invoker.model.TestResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-11
 */
@Slf4j
public class Handler<I, O, T> {
    final Invoker<I, O, T> invoker;
    final List<CallHandler<I, O, T>> handlers = new ArrayList<>();

    Handler(Invoker<I, O, T> invoker) {
        this.invoker = invoker;
    }

    void addHandler(CallHandler<I, O, T> handler) {
        this.handlers.add(handler);
    }


    T handleResult(Throwable t, O res, HandleInfo handleInfo) {
        Recorder recorder = invoker.recorder;
        Supplier<T> defaultValue = invoker.defaultValue;
        if (t != null && recorder != null && recorder.exception) {
            InvokerCallManager.addDowngradeInfo(new ExceptionDowngradeInfo(invoker.getRealScene(), invoker.desc.getName(), "异常降级"));
        }
        for (CallHandler<I, O, T> handler : handlers) {
            if (handlerTypeNotMatch(handler, t)) {
                continue;
            }
            TestResult testResult = handler.test(t, res);
            if (testResult.isMatch()) {
                if (testResult.isOnFail()) {
                    handleInfo.setHasFail(true);
                }
                try {
                    T handleRes = handler.action(t, res);
                    if (handleRes == null && defaultValue != null) {
                        handleInfo.setReturnDefaultReason("handler处理结果为空");
                        return defaultValue.get();
                    }
                    return handleRes;
                } catch (RuntimeException re) {
                    throw re;
                } catch (Exception e) {
                    throw new InvokerCallException(e);
                }
            }
        }
        if (t != null) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new InvokerCallException(t);
        }

        try {
            return invoker.caller.processor.apply(res);
        } catch (Throwable pt) {
            if (defaultValue != null) {
                log.error("[fail-sale] processor exception, returnDefaultValue", pt);
                // 设置了defaultValue，兜底异常
                handleInfo.setReturnDefaultReason("processor处理过程异常");
                return defaultValue.get();
            }
            throw pt;
        }
    }

    private boolean handlerTypeNotMatch(CallHandler<I, O, T> handler, Throwable t) {
        if (handler instanceof ExResultHandler) {
            return false;
        }
        if (t != null) {
            return !(handler instanceof ExceptionHandler);
        }
        return !(handler instanceof ResultHandler);
    }

    StrongType getStrongType() {
        if (invoker.desc != null && invoker.desc.getStrongType() != null) {
            return invoker.desc.getStrongType();
        }
        if (CollectionUtils.isEmpty(handlers)) {
            return StrongType.STRONG;
        }
        if (this.handlers.stream().allMatch(CallHandler::isStrong)) {
            return StrongType.STRONG;
        }
        if (this.handlers.stream().noneMatch(CallHandler::isStrong)) {
            return StrongType.WEAK;
        }
        return StrongType.CONDITION_WAK;
    }
}
