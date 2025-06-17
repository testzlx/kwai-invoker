package com.kuaishou.invoker.async;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

/**
 * @author liuzhifeng <liuzhifeng03@kuaishou.com>
 * Created on 2025-03-24
 */
@Slf4j
public class BusinessFlowManagerUtils {

    private static Method getCurrentExecutionUnit = null;

    private static Method setCurrentExecutionUnit = null;

    static {
        try {
            getCurrentExecutionUnit =
                    Class.forName("com.kuaishou.ad.merchant.framework.template.BusinessFlowManager")
                            .getMethod("getCurrentExecutionUnit");
            setCurrentExecutionUnit =
                    Class.forName("com.kuaishou.ad.merchant.framework.template.BusinessFlowManager")
                            .getMethod("setCurrentExecutionUnit", String.class);
        } catch (Throwable ignored) {
        }
    }


    public static String getCurrentExecutionUnit() {
        String errorMsg = "获取异步调用所在的执行单元失败，需确认是否引入了kuaishou-merchant-execution-framework依赖";
        if (getCurrentExecutionUnit == null) {
            log.error(errorMsg);
            return null;
        }
        try {
            Object result = getCurrentExecutionUnit.invoke(null);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (Throwable t) {
            log.error(errorMsg, t);
        }
        return null;
    }

    public static void setCurrentExecutionUnit(String curExecutionUnit) {
        String errorMsg = String.format(
                "设置异步调用所在的执行单元失败，curExecutionUnit:%s，需确认是否引入了kuaishou-merchant-execution-framework依赖",
                curExecutionUnit);
        if (setCurrentExecutionUnit == null) {
            log.error(errorMsg);
            return;
        }
        try {
            setCurrentExecutionUnit.invoke(null, curExecutionUnit);
        } catch (Throwable t) {
            log.error(errorMsg, t);
        }
    }
}
