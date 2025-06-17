package com.kuaishou.invoker;

import lombok.Getter;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-18
 */
@Getter
public class Recorder {
    boolean downgrade;
    boolean exception;

    private Recorder() {
    }

    public static Recorder inst() {
        return new Recorder();
    }

    public Recorder onDowngrade() {
        downgrade = true;
        return this;
    }

    public Recorder onException() {
        exception = true;
        return this;
    }
}
