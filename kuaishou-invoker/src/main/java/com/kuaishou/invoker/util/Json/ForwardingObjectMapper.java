package com.kuaishou.invoker.util.Json;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.DateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MutableConfigOverride;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author liuzhifeng <liuzhifeng03@kuaishou.com>
 * Created on 2025-05-09
 */
public class ForwardingObjectMapper extends ObjectMapper {

    private final ObjectMapper delegate;

    public ForwardingObjectMapper(ObjectMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public ObjectMapper copy() {
        return delegate.copy();
    }

    @Override
    public Version version() {
        return delegate.version();
    }

    @Override
    public ObjectMapper registerModule(Module module) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper registerModules(Module... modules) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper registerModules(Iterable<? extends Module> modules) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public Set<Object> getRegisteredModuleIds() {
        return delegate.getRegisteredModuleIds();
    }

    public static List<Module> findModules() {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    public static List<Module> findModules(ClassLoader classLoader) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper findAndRegisterModules() {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public SerializationConfig getSerializationConfig() {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public DeserializationConfig getDeserializationConfig() {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public DeserializationContext getDeserializationContext() {
        return delegate.getDeserializationContext();
    }

    @Override
    public ObjectMapper setSerializerFactory(SerializerFactory f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public SerializerFactory getSerializerFactory() {
        return delegate.getSerializerFactory();
    }

    @Override
    public ObjectMapper setSerializerProvider(DefaultSerializerProvider p) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public SerializerProvider getSerializerProvider() {
        return delegate.getSerializerProvider();
    }

    @Override
    public SerializerProvider getSerializerProviderInstance() {
        return delegate.getSerializerProviderInstance();
    }

    @Override
    public ObjectMapper setMixIns(Map<Class<?>, Class<?>> sourceMixins) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper addMixIn(Class<?> target, Class<?> mixinSource) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setMixInResolver(ClassIntrospector.MixInResolver resolver) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public Class<?> findMixInClassFor(Class<?> cls) {
        return delegate.findMixInClassFor(cls);
    }

    @Override
    public int mixInCount() {
        return delegate.mixInCount();
    }

    @Override
    @Deprecated
    public void setMixInAnnotations(Map<Class<?>, Class<?>> sourceMixins) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public VisibilityChecker<?> getVisibilityChecker() {
        return delegate.getVisibilityChecker();
    }

    @Override
    public ObjectMapper setVisibility(VisibilityChecker<?> vc) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setVisibility(PropertyAccessor forMethod, JsonAutoDetect.Visibility visibility) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public SubtypeResolver getSubtypeResolver() {
        return delegate.getSubtypeResolver();
    }

    @Override
    public ObjectMapper setSubtypeResolver(SubtypeResolver str) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setAnnotationIntrospector(AnnotationIntrospector ai) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setAnnotationIntrospectors(AnnotationIntrospector serializerAI, AnnotationIntrospector deserializerAI) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setPropertyNamingStrategy(PropertyNamingStrategy s) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public PropertyNamingStrategy getPropertyNamingStrategy() {
        return delegate.getPropertyNamingStrategy();
    }

    @Override
    public ObjectMapper setDefaultPrettyPrinter(PrettyPrinter pp) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    @Deprecated
    public void setVisibilityChecker(VisibilityChecker<?> vc) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setSerializationInclusion(JsonInclude.Include incl) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    @Deprecated
    public ObjectMapper setPropertyInclusion(JsonInclude.Value incl) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setDefaultPropertyInclusion(JsonInclude.Value incl) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setDefaultPropertyInclusion(JsonInclude.Include incl) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setDefaultSetterInfo(JsonSetter.Value v) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setDefaultVisibility(JsonAutoDetect.Value vis) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setDefaultMergeable(Boolean b) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enableDefaultTyping() {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enableDefaultTyping(DefaultTyping dti) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enableDefaultTyping(DefaultTyping applicability, JsonTypeInfo.As includeAs) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enableDefaultTypingAsProperty(DefaultTyping applicability, String propertyName) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper disableDefaultTyping() {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setDefaultTyping(TypeResolverBuilder<?> typer) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public void registerSubtypes(Class<?>... classes) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public void registerSubtypes(NamedType... types) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public void registerSubtypes(Collection<Class<?>> subtypes) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public MutableConfigOverride configOverride(Class<?> type) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public TypeFactory getTypeFactory() {
        return delegate.getTypeFactory();
    }

    @Override
    public ObjectMapper setTypeFactory(TypeFactory f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public JavaType constructType(Type t) {
        return delegate.constructType(t);
    }

    @Override
    public JsonNodeFactory getNodeFactory() {
        return delegate.getNodeFactory();
    }

    @Override
    public ObjectMapper setNodeFactory(JsonNodeFactory f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper addHandler(DeserializationProblemHandler h) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper clearProblemHandlers() {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setConfig(DeserializationConfig config) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    @Deprecated
    public void setFilters(FilterProvider filterProvider) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setFilterProvider(FilterProvider filterProvider) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setBase64Variant(Base64Variant v) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setConfig(SerializationConfig config) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public JsonFactory getFactory() {
        return delegate.getFactory();
    }

    @Override
    @Deprecated
    public JsonFactory getJsonFactory() {
        return delegate.getJsonFactory();
    }

    @Override
    public ObjectMapper setDateFormat(DateFormat dateFormat) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public DateFormat getDateFormat() {
        return delegate.getDateFormat();
    }

    @Override
    public Object setHandlerInstantiator(HandlerInstantiator hi) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setInjectableValues(InjectableValues injectableValues) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public InjectableValues getInjectableValues() {
        return delegate.getInjectableValues();
    }

    @Override
    public ObjectMapper setLocale(Locale l) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper setTimeZone(TimeZone tz) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public boolean isEnabled(MapperFeature f) {
        return delegate.isEnabled(f);
    }

    @Override
    public ObjectMapper configure(MapperFeature f, boolean state) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enable(MapperFeature... f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper disable(MapperFeature... f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public boolean isEnabled(SerializationFeature f) {
        return delegate.isEnabled(f);
    }

    @Override
    public ObjectMapper configure(SerializationFeature f, boolean state) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enable(SerializationFeature f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enable(SerializationFeature first, SerializationFeature... f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper disable(SerializationFeature f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper disable(SerializationFeature first, SerializationFeature... f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public boolean isEnabled(DeserializationFeature f) {
        return delegate.isEnabled(f);
    }

    @Override
    public ObjectMapper configure(DeserializationFeature f, boolean state) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enable(DeserializationFeature feature) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enable(DeserializationFeature first, DeserializationFeature... f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper disable(DeserializationFeature feature) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper disable(DeserializationFeature first, DeserializationFeature... f) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public boolean isEnabled(JsonParser.Feature f) {
        return delegate.isEnabled(f);
    }

    @Override
    public ObjectMapper configure(JsonParser.Feature f, boolean state) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enable(JsonParser.Feature... features) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper disable(JsonParser.Feature... features) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public boolean isEnabled(JsonGenerator.Feature f) {
        return delegate.isEnabled(f);
    }

    @Override
    public ObjectMapper configure(JsonGenerator.Feature f, boolean state) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper enable(JsonGenerator.Feature... features) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public ObjectMapper disable(JsonGenerator.Feature... features) {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public boolean isEnabled(JsonFactory.Feature f) {
        return delegate.isEnabled(f);
    }

    @Override
    public <T> T readValue(JsonParser p, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(p, valueType);
    }

    @Override
    public <T> T readValue(JsonParser p, TypeReference<?> valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(p, valueTypeRef);
    }

    @Override
    public <T> T readValue(JsonParser p, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(p, valueType);
    }

    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException, JsonProcessingException {
        return delegate.readTree(p);
    }

    @Override
    public <T> MappingIterator<T> readValues(JsonParser p, ResolvedType valueType) throws IOException, JsonProcessingException {
        return delegate.readValues(p, valueType);
    }

    @Override
    public <T> MappingIterator<T> readValues(JsonParser p, JavaType valueType) throws IOException, JsonProcessingException {
        return delegate.readValues(p, valueType);
    }

    @Override
    public <T> MappingIterator<T> readValues(JsonParser p, Class<T> valueType) throws IOException, JsonProcessingException {
        return delegate.readValues(p, valueType);
    }

    @Override
    public <T> MappingIterator<T> readValues(JsonParser p, TypeReference<?> valueTypeRef) throws IOException, JsonProcessingException {
        return delegate.readValues(p, valueTypeRef);
    }

    @Override
    public JsonNode readTree(InputStream in) throws IOException {
        return delegate.readTree(in);
    }

    @Override
    public JsonNode readTree(Reader r) throws IOException {
        return delegate.readTree(r);
    }

    @Override
    public JsonNode readTree(String content) throws IOException {
        return delegate.readTree(content);
    }

    @Override
    public JsonNode readTree(byte[] content) throws IOException {
        return delegate.readTree(content);
    }

    @Override
    public JsonNode readTree(File file) throws IOException, JsonProcessingException {
        return delegate.readTree(file);
    }

    @Override
    public JsonNode readTree(URL source) throws IOException {
        return delegate.readTree(source);
    }

    @Override
    public void writeValue(JsonGenerator g, Object value) throws IOException, JsonGenerationException, JsonMappingException {
        delegate.writeValue(g, value);
    }

    @Override
    public void writeTree(JsonGenerator jgen, TreeNode rootNode) throws IOException, JsonProcessingException {
        delegate.writeTree(jgen, rootNode);
    }

    @Override
    public void writeTree(JsonGenerator jgen, JsonNode rootNode) throws IOException, JsonProcessingException {
        delegate.writeTree(jgen, rootNode);
    }

    @Override
    public ObjectNode createObjectNode() {
        return delegate.createObjectNode();
    }

    @Override
    public ArrayNode createArrayNode() {
        return delegate.createArrayNode();
    }

    @Override
    public JsonParser treeAsTokens(TreeNode n) {
        return delegate.treeAsTokens(n);
    }

    @Override
    public <T> T treeToValue(TreeNode n, Class<T> valueType) throws JsonProcessingException {
        return delegate.treeToValue(n, valueType);
    }

    @Override
    public <T extends JsonNode> T valueToTree(Object fromValue) throws IllegalArgumentException {
        return delegate.valueToTree(fromValue);
    }

    @Override
    public boolean canSerialize(Class<?> type) {
        return delegate.canSerialize(type);
    }

    @Override
    public boolean canSerialize(Class<?> type, AtomicReference<Throwable> cause) {
        return delegate.canSerialize(type, cause);
    }

    @Override
    public boolean canDeserialize(JavaType type) {
        return delegate.canDeserialize(type);
    }

    @Override
    public boolean canDeserialize(JavaType type, AtomicReference<Throwable> cause) {
        return delegate.canDeserialize(type, cause);
    }

    @Override
    public <T> T readValue(File src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(File src, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueTypeRef);
    }

    @Override
    public <T> T readValue(File src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(URL src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(URL src, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueTypeRef);
    }

    @Override
    public <T> T readValue(URL src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(String content, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(content, valueType);
    }

    @Override
    public <T> T readValue(String content, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(content, valueTypeRef);
    }

    @Override
    public <T> T readValue(String content, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(content, valueType);
    }

    @Override
    public <T> T readValue(Reader src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(Reader src, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueTypeRef);
    }

    @Override
    public <T> T readValue(Reader src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(InputStream src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(InputStream src, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueTypeRef);
    }

    @Override
    public <T> T readValue(InputStream src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(byte[] src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(byte[] src, int offset, int len, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, offset, len, valueType);
    }

    @Override
    public <T> T readValue(byte[] src, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueTypeRef);
    }

    @Override
    public <T> T readValue(byte[] src, int offset, int len, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, offset, len, valueTypeRef);
    }

    @Override
    public <T> T readValue(byte[] src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(byte[] src, int offset, int len, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return delegate.readValue(src, offset, len, valueType);
    }

    @Override
    public <T> T readValue(DataInput src, Class<T> valueType) throws IOException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(DataInput src, JavaType valueType) throws IOException {
        return delegate.readValue(src, valueType);
    }

    @Override
    public void writeValue(File resultFile, Object value) throws IOException, JsonGenerationException, JsonMappingException {
        delegate.writeValue(resultFile, value);
    }

    @Override
    public void writeValue(OutputStream out, Object value) throws IOException, JsonGenerationException, JsonMappingException {
        delegate.writeValue(out, value);
    }

    @Override
    public void writeValue(DataOutput out, Object value) throws IOException {
        delegate.writeValue(out, value);
    }

    @Override
    public void writeValue(Writer w, Object value) throws IOException, JsonGenerationException, JsonMappingException {
        delegate.writeValue(w, value);
    }

    @Override
    public String writeValueAsString(Object value) throws JsonProcessingException {
        return delegate.writeValueAsString(value);
    }

    @Override
    public byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
        return delegate.writeValueAsBytes(value);
    }

    @Override
    public ObjectWriter writer() {
        return delegate.writer();
    }

    @Override
    public ObjectWriter writer(SerializationFeature feature) {
        return delegate.writer(feature);
    }

    @Override
    public ObjectWriter writer(SerializationFeature first, SerializationFeature... other) {
        return delegate.writer(first, other);
    }

    @Override
    public ObjectWriter writer(DateFormat df) {
        return delegate.writer(df);
    }

    @Override
    public ObjectWriter writerWithView(Class<?> serializationView) {
        return delegate.writerWithView(serializationView);
    }

    @Override
    public ObjectWriter writerFor(Class<?> rootType) {
        return delegate.writerFor(rootType);
    }

    @Override
    public ObjectWriter writerFor(TypeReference<?> rootType) {
        return delegate.writerFor(rootType);
    }

    @Override
    public ObjectWriter writerFor(JavaType rootType) {
        return delegate.writerFor(rootType);
    }

    @Override
    public ObjectWriter writer(PrettyPrinter pp) {
        return delegate.writer(pp);
    }

    @Override
    public ObjectWriter writerWithDefaultPrettyPrinter() {
        return delegate.writerWithDefaultPrettyPrinter();
    }

    @Override
    public ObjectWriter writer(FilterProvider filterProvider) {
        return delegate.writer(filterProvider);
    }

    @Override
    public ObjectWriter writer(FormatSchema schema) {
        return delegate.writer(schema);
    }

    @Override
    public ObjectWriter writer(Base64Variant defaultBase64) {
        return delegate.writer(defaultBase64);
    }

    @Override
    public ObjectWriter writer(CharacterEscapes escapes) {
        return delegate.writer(escapes);
    }

    @Override
    public ObjectWriter writer(ContextAttributes attrs) {
        return delegate.writer(attrs);
    }

    @Override
    @Deprecated
    public ObjectWriter writerWithType(Class<?> rootType) {
        return delegate.writerWithType(rootType);
    }

    @Override
    @Deprecated
    public ObjectWriter writerWithType(TypeReference<?> rootType) {
        return delegate.writerWithType(rootType);
    }

    @Override
    @Deprecated
    public ObjectWriter writerWithType(JavaType rootType) {
        return delegate.writerWithType(rootType);
    }

    @Override
    public ObjectReader reader() {
        return delegate.reader();
    }

    @Override
    public ObjectReader reader(DeserializationFeature feature) {
        return delegate.reader(feature);
    }

    @Override
    public ObjectReader reader(DeserializationFeature first, DeserializationFeature... other) {
        return delegate.reader(first, other);
    }

    @Override
    public ObjectReader readerForUpdating(Object valueToUpdate) {
        return delegate.readerForUpdating(valueToUpdate);
    }

    @Override
    public ObjectReader readerFor(JavaType type) {
        return delegate.readerFor(type);
    }

    @Override
    public ObjectReader readerFor(Class<?> type) {
        return delegate.readerFor(type);
    }

    @Override
    public ObjectReader readerFor(TypeReference<?> type) {
        return delegate.readerFor(type);
    }

    @Override
    public ObjectReader reader(JsonNodeFactory f) {
        return delegate.reader(f);
    }

    @Override
    public ObjectReader reader(FormatSchema schema) {
        return delegate.reader(schema);
    }

    @Override
    public ObjectReader reader(InjectableValues injectableValues) {
        return delegate.reader(injectableValues);
    }

    @Override
    public ObjectReader readerWithView(Class<?> view) {
        return delegate.readerWithView(view);
    }

    @Override
    public ObjectReader reader(Base64Variant defaultBase64) {
        return delegate.reader(defaultBase64);
    }

    @Override
    public ObjectReader reader(ContextAttributes attrs) {
        return delegate.reader(attrs);
    }

    @Override
    @Deprecated
    public ObjectReader reader(JavaType type) {
        return delegate.reader(type);
    }

    @Override
    @Deprecated
    public ObjectReader reader(Class<?> type) {
        return delegate.reader(type);
    }

    @Override
    @Deprecated
    public ObjectReader reader(TypeReference<?> type) {
        return delegate.reader(type);
    }

    @Override
    public <T> T convertValue(Object fromValue, Class<T> toValueType) throws IllegalArgumentException {
        return delegate.convertValue(fromValue, toValueType);
    }

    @Override
    public <T> T convertValue(Object fromValue, TypeReference<?> toValueTypeRef) throws IllegalArgumentException {
        return delegate.convertValue(fromValue, toValueTypeRef);
    }

    @Override
    public <T> T convertValue(Object fromValue, JavaType toValueType) throws IllegalArgumentException {
        return delegate.convertValue(fromValue, toValueType);
    }

    @Override
    public <T> T updateValue(T valueToUpdate, Object overrides) throws JsonMappingException {
        return delegate.updateValue(valueToUpdate, overrides);
    }

    @Override
    @Deprecated
    public JsonSchema generateJsonSchema(Class<?> t) throws JsonMappingException {
        return delegate.generateJsonSchema(t);
    }

    @Override
    public void acceptJsonFormatVisitor(Class<?> type, JsonFormatVisitorWrapper visitor) throws JsonMappingException {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }

    @Override
    public void acceptJsonFormatVisitor(JavaType type, JsonFormatVisitorWrapper visitor) throws JsonMappingException {
        throw new UnsupportedOperationException("create new ObjectMapper instance when using this method");
    }
}
