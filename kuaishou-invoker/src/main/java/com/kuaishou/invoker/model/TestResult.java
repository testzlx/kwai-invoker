package com.kuaishou.invoker.model;

import lombok.Data;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-25
 */
@Data
public class TestResult {
    private boolean match;

    public TestResult(boolean match) {
        this.match = match;
    }

    private boolean onFail;
    private boolean onException;

    public TestResult onFail() {
        this.onFail = true;
        return this;
    }

    public TestResult onException() {
        this.onException = true;
        return this;
    }
}
