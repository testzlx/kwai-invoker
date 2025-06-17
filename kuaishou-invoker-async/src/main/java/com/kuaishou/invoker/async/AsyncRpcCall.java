package com.kuaishou.invoker.async;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2022-03-06
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncRpcCall {

    /**
     * 是否在执行单元中发起下游异步调用，
     * true——是，需要获取当前执行单元信息
     */
    boolean withExecutionUnit() default false;

}
