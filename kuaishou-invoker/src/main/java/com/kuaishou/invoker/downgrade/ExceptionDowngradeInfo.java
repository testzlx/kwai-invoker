package com.kuaishou.invoker.downgrade;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-18
 */
@Data
@AllArgsConstructor
public class ExceptionDowngradeInfo {
    private String scene;
    private String code;
    private String desc;
}
