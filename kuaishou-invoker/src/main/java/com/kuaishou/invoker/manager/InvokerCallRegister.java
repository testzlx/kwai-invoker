package com.kuaishou.invoker.manager;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-03-02
 */
public class InvokerCallRegister {
    private static volatile String domainCode = "trade.order";

    public static void registerDomain(String domainCode) {
        InvokerCallRegister.domainCode = domainCode;
    }

    public static String getPerfDomain() {
        return String.format("invoker.%s", InvokerCallRegister.domainCode);
    }
}
