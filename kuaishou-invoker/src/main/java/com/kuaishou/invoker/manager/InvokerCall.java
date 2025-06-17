package com.kuaishou.invoker.manager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2022-03-06
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface InvokerCall {
    String scene();
}
