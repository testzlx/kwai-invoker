package com.kuaishou.invoker.model;

import lombok.Data;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-08-03
 */
@Data
public class HandleInfo {
    // 是否调用失败
    private boolean hasFail;
    // 返回声明默认值的原因
    private String returnDefaultReason;
}
