package com.kuaishou.invoker.util.Json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author liuzhifeng <liuzhifeng03@kuaishou.com>
 * Created on 2025-05-09
 */
public class JsonMapConverter {
    private final Map<String, Object> dataMap;

    protected <T> T getData(String dataKey, Class<T> tClass) {
        Object object = dataMap.get(dataKey);
        if (object == null) {
            return null;
        }
        return JsonUtil.of(JsonUtil.of(object), tClass);
    }

    protected <T> T getData(String dataKey, Class<T> tClass, T defaultValue) {
        Object object = dataMap.get(dataKey);
        if (object == null) {
            return defaultValue;
        }
        return JsonUtil.of(JsonUtil.of(object), tClass);
    }

    protected <E, T extends Collection<E>> T getData(String dataKey, Class<? extends Collection> collectionType,
                                                     Class<E> tClass) {
        Object object = dataMap.get(dataKey);
        if (object == null) {
            return null;
        }
        return JsonUtil.ofCollection(JsonUtil.of(object), collectionType, tClass);
    }


    protected <K, V> Map<K, V> getMapData(String dataKey, Class<K> keyClass, Class<V> valueClass) {
        Object object = dataMap.get(dataKey);
        if (object == null) {
            return null;
        }
        return JsonUtil.ofMap(JsonUtil.of(object), keyClass, valueClass);
    }

    protected <T> void setData(String dataKey, T object) {
        dataMap.put(dataKey, object);
    }

    public JsonMapConverter(String data) {
        if (StringUtils.isBlank(data)) {
            dataMap = new HashMap<>();
        } else {
            this.dataMap = MapUtils.emptyIfNull(
                    JsonUtil.ofMap(data, String.class, Object.class));
        }
    }

    @JsonIgnore
    public String getDataStr() {
        return JsonUtil.of(dataMap);
    }
}
