package com.kuaishou.invoker.util.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

/**
 * @author liuzhifeng <liuzhifeng03@kuaishou.com>
 * Created on 2025-05-09
 */
public class JsonMapper {
    private static final ObjectMapper MAPPER = ObjectMapperUtils.mapper().copy();

    static {
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)                // 默认为true
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)        // 默认为true
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)       // 默认为false
                .configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true)                    // 默认为false
                .configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)    // 默认为false
                .configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true)                // 默认为false
                .configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true)            // 默认为false
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)            // 默认为false
                .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)                    // 默认为false
                .configure(JsonParser.Feature.IGNORE_UNDEFINED, true)                         // 默认为false
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        MAPPER.registerModule(new AfterburnerModule());
        MAPPER.registerModule(new GuavaModule());
    }

    public static List<Module> findModules() {
        return findModules(null);
    }

    public static List<Module> findModules(ClassLoader classLoader) {
        ArrayList<Module> modules = new ArrayList<>();
        ServiceLoader<Module> loader = (classLoader == null) ? ServiceLoader.load(Module.class)
                : ServiceLoader.load(Module.class, classLoader);
        for (Module module : loader) {
            modules.add(module);
        }
        return modules;
    }


    public Version version() {
        return MAPPER.version();
    }

    public TypeFactory getTypeFactory() {
        return MAPPER.getTypeFactory();
    }

    public <T> T readValue(String content, Class<T> valueType) throws JsonException {
        try {
            return MAPPER.readValue(content, valueType);
        } catch (Exception e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    public <T> T readValue(String content, JavaType valueType) throws JsonException {
        try {
            return MAPPER.readValue(content, valueType);
        } catch (Exception e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    public <T> T readerFor(String json, TypeReference<T> reference) {
        try {
            return MAPPER.readerFor(reference).readValue(json);
        } catch (Exception e) {
            throw new JsonException(e.getMessage(), e);
        }
    }


    public String writeValueAsString(Object value) throws JsonException {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new JsonException(e.getMessage(), e);
        }
    }
}
