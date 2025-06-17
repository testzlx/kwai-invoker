package com.kuaishou.invoker.async;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2022-03-06
 */
@Slf4j
@Aspect
@Component
@EnableAspectJAutoProxy
public class AsyncRpcCallAspect {

    @Pointcut("@annotation(com.kuaishou.invoker.async.AsyncRpcCall)")
    public void asyncRpcCallAspect() {

    }

    @Around(value = "asyncRpcCallAspect()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        AsyncRpcCall annotation = method.getAnnotation(AsyncRpcCall.class);
        boolean withExecutionUnit = annotation.withExecutionUnit();
        AsyncRpcCallManager.start(withExecutionUnit);
        try {
            Object result = pjp.proceed();
            AsyncRpcCallManager.end();
            return result;
        } catch (Throwable t) {
            AsyncRpcCallManager.cancel();
            throw t;
        }
    }
}