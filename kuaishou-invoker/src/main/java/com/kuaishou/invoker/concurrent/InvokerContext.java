package com.kuaishou.invoker.concurrent;

import java.util.List;

import org.apache.commons.collections4.ListUtils;

import com.kuaishou.invoker.async.AsyncRpcCallManager;
import com.kuaishou.invoker.downgrade.ExceptionDowngradeInfo;
import com.kuaishou.invoker.manager.InvokerCallManager;

import lombok.Data;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2024-02-25
 */
@Data
public class InvokerContext {
    private String scene;
    private List<ExceptionDowngradeInfo> downgradeInfoList;
    private AsyncRpcCallManager.LazySupplierContext lazySupplierContext;

    public static InvokerContext getCurrentContext() {
        InvokerContext invokerContext = new InvokerContext();
        invokerContext.setScene(InvokerCallManager.getScene());
        invokerContext.setDowngradeInfoList(InvokerCallManager.getDowngradeInfoList());
        invokerContext.setLazySupplierContext(AsyncRpcCallManager.getLazyContext());
        return invokerContext;
    }

    public void setCurrentContext() {
        InvokerCallManager.start(this.scene);
        ListUtils.emptyIfNull(this.getDowngradeInfoList()).forEach(InvokerCallManager::addDowngradeInfo);
        AsyncRpcCallManager.LazySupplierContext lazySupplierContext = this.getLazySupplierContext();
        if (lazySupplierContext != null && lazySupplierContext.isStarted()) {
            AsyncRpcCallManager.start(lazySupplierContext.isWithExecutionUnit());
            ListUtils.emptyIfNull(lazySupplierContext.getLazyList()).forEach(AsyncRpcCallManager::add);
        }
    }

    public void removeCurrentContext() {
        InvokerCallManager.end();
        AsyncRpcCallManager.LazySupplierContext lazySupplierContext = this.getLazySupplierContext();
        if (lazySupplierContext != null && lazySupplierContext.isStarted()) {
            AsyncRpcCallManager.remove();
        }
    }
}
