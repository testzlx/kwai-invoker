package com.kuaishou.invoker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultErrorInfo {
    private String errorCode;
    private String errorMsg;
}
