package com.kuaishou.invoker.manager;

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
public class InvokerCallAspect {

    public static final String TOTAL_PROCESS = "totalProcess";

    @Pointcut("@annotation(com.kuaishou.invoker.manager.InvokerCall)")
    public void invokerCallAspect() {

    }

    @Around(value = "invokerCallAspect()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        InvokerCall annotation = method.getAnnotation(InvokerCall.class);
        String scene = annotation.scene();
        InvokerCallManager.start(scene);
        try {
            return pjp.proceed();
        } finally {
            InvokerCallManager.end();
        }
    }
}