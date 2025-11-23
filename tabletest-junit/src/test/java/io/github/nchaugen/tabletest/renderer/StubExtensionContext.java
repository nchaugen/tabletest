package io.github.nchaugen.tabletest.renderer;

import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("NullableProblems")
record StubExtensionContext(Map<String, String> config) implements ExtensionContext {

    public StubExtensionContext() {
        this(Collections.emptyMap());
    }

    @Override
    public Optional<ExtensionContext> getParent() {
        return Optional.empty();
    }

    @Override
    public ExtensionContext getRoot() {
        return null;
    }

    @Override
    public String getUniqueId() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "Display Name";
    }

    @Override
    public Set<String> getTags() {
        return Set.of();
    }

    @Override
    public Optional<AnnotatedElement> getElement() {
        return Optional.empty();
    }

    @Override
    public Optional<Class<?>> getTestClass() {
        return Optional.empty();
    }

    @Override
    public List<Class<?>> getEnclosingTestClasses() {
        return List.of();
    }

    @Override
    public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
        return Optional.empty();
    }

    @Override
    public Optional<Object> getTestInstance() {
        return Optional.empty();
    }

    @Override
    public Optional<TestInstances> getTestInstances() {
        return Optional.empty();
    }

    @Override
    public Optional<Method> getTestMethod() {
        return Optional.empty();
    }

    @Override
    public Optional<Throwable> getExecutionException() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getConfigurationParameter(String key) {
        return Optional.ofNullable(config.get(key));
    }

    @Override
    public <T> Optional<T> getConfigurationParameter(String key, Function<? super String, ? extends T> transformer) {
        return Optional.empty();
    }

    @Override
    public void publishReportEntry(Map<String, String> map) {
    }

    @Override
    public void publishFile(String name, MediaType mediaType, ThrowingConsumer<Path> action) {
    }

    @Override
    public void publishDirectory(String name, ThrowingConsumer<Path> action) {
    }

    @Override
    public Store getStore(Namespace namespace) {
        return null;
    }

    @Override
    public Store getStore(StoreScope scope, Namespace namespace) {
        return null;
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return null;
    }

    @Override
    public ExecutableInvoker getExecutableInvoker() {
        return null;
    }
}
