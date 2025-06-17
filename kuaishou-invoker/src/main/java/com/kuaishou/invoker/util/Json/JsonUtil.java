package com.kuaishou.invoker.util.Json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.base.Strings;

/**
 * @author liuzhifeng <liuzhifeng03@kuaishou.com>
 * Created on 2025-05-09
 */
public class JsonUtil {
    private static final JsonMapper JSON_MAPPER = new JsonMapper();


    public static String of(Object o) {
        return JSON_MAPPER.writeValueAsString(o);
    }

    public static String of(JsonMapConverter o) {
        return toJson(o);
    }

    public static <T> T of(String json, Class<T> tClass) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        return JSON_MAPPER.readValue(json, tClass);
    }

    public static <T> T of(String json, TypeReference<T> reference) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        return JSON_MAPPER.readerFor(json, reference);
    }

    public static <T> List<T> ofList(String json, Class<T> tClass) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        JavaType javaType = JSON_MAPPER.getTypeFactory().constructParametricType(ArrayList.class, tClass);
        return JSON_MAPPER.readValue(json, javaType);
    }

    public static <K, V> Map<K, V> ofMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        MapType mapType = JSON_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);
        return JSON_MAPPER.readValue(json, mapType);
    }

    public static String toJson(Object obj) {
        if (null == obj) {
            return null;
        }
        return JSON_MAPPER.writeValueAsString(obj);
    }

    public static String toJson(JsonMapConverter obj) {
        if (null == obj) {
            return null;
        }
        return obj.getDataStr();
    }

    public static <E, T extends Collection<E>> T ofCollection(String json, Class<? extends Collection> collectionType,
                                                              Class<E> valueType) {
        if (StringUtils.isEmpty(json)) {
            json = "[]";
        }
        JavaType javaType = JSON_MAPPER.getTypeFactory().constructCollectionType(collectionType, valueType);
        return JSON_MAPPER.readValue(json, javaType);
    }
}
