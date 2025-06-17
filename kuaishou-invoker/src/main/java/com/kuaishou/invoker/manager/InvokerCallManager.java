package com.kuaishou.invoker.manager;

import com.google.common.collect.Maps;
import com.kuaishou.invoker.downgrade.ExceptionDowngradeInfo;


import java.util.*;

/**
 * @author zhangqinxian <zhangqinxian@kuaishou.com>
 * Created on 2022-02-07
 */
public class InvokerCallManager {
    private static final ThreadLocal<String> DOMAIN_NAME_CONTAINER = ThreadLocal.withInitial(() -> "未知");
    private static final ThreadLocal<Map<String, ExceptionDowngradeInfo>> EXCEPTION_DOWNGRADE_HOLDER = ThreadLocal.withInitial(HashMap::new);

    public static void start(String scene) {
        DOMAIN_NAME_CONTAINER.set(scene);
        EXCEPTION_DOWNGRADE_HOLDER.set(Maps.newHashMap());
    }

    public static void end() {
        DOMAIN_NAME_CONTAINER.remove();
        EXCEPTION_DOWNGRADE_HOLDER.remove();
    }

    public static String getScene() {
        return DOMAIN_NAME_CONTAINER.get();
    }

    public static void addDowngradeInfo(ExceptionDowngradeInfo downgradeInfo) {
        if (EXCEPTION_DOWNGRADE_HOLDER.get() == null) {
            return;
        }
        EXCEPTION_DOWNGRADE_HOLDER.get().put(getFullName(downgradeInfo), downgradeInfo);
    }

    public static List<ExceptionDowngradeInfo> getDowngradeInfoList() {
        if (EXCEPTION_DOWNGRADE_HOLDER.get() == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(EXCEPTION_DOWNGRADE_HOLDER.get().values());
    }

    private static String getFullName(ExceptionDowngradeInfo downgradeInfo) {
        return String.format("%s.%s", downgradeInfo.getScene(), downgradeInfo.getCode());
    }
}
