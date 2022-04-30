package com.github.writingbettercodethanyou.gamerpluginframework.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ServiceRegistry {

    public static class Builder {

        private final Map<Class<?>, Function<ServiceRegistry, ?>> bindingMap = new HashMap<>();

        public ServiceRegistry build() {
            return new ServiceRegistry(bindingMap);
        }

        public <T> Builder addTransient(Class<T> targetClass) {
            return addTransient(targetClass, targetClass);
        }

        public <T> Builder addTransient(Class<T> targetClass, Class<? extends T> implementationClass) {
            bindingMap.put(targetClass, (serviceRegistry) -> serviceRegistry.createInstance(implementationClass));
            return this;
        }

        public <T> Builder addSingleton(Class<T> targetClass) {
            return addSingleton(targetClass, targetClass);
        }

        public <T> Builder addSingleton(Class<T> targetClass, Class<? extends T> implementationClass) {
            bindingMap.put(targetClass, new Function<ServiceRegistry, T>() {
                private T instance;

                @Override
                public T apply(ServiceRegistry serviceRegistry) {
                    return (instance == null ? instance = serviceRegistry.createInstance(implementationClass) : instance);
                }
            });
            return this;
        }

        public <T> Builder addSingleton(T implementation) {
            bindingMap.put(implementation.getClass(), (serviceRegistry) -> implementation);
            return this;
        }

        public <T> Builder addSingleton(Class<T> targetClass, T implementation) {
            bindingMap.put(targetClass, (serviceRegistry) -> implementation);
            return this;
        }

        public <T> Builder addSupplied(Class<T> targetClass, Supplier<T> supplier) {
            bindingMap.put(targetClass, (serviceRegistry) -> supplier.get());
            return this;
        }
    }

    private final Map<Class<?>, Function<ServiceRegistry, ?>> bindingMap;

    public ServiceRegistry(Map<Class<?>, Function<ServiceRegistry, ?>> bindingMap) {
        this.bindingMap = bindingMap;
        bindingMap.computeIfAbsent(ServiceRegistry.class, (k) -> (serviceRegistry) -> serviceRegistry);
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> classs) {
        for (Constructor<?> constructor : classs.getConstructors()) {
            if (!Arrays.stream(constructor.getParameterTypes()).allMatch(bindingMap::containsKey))
                continue;

            Object[] parameters = Arrays.stream(constructor.getParameterTypes()).map(bindingMap::get).map((func) -> func.apply(this)).toArray();

            try {
                return (T) constructor.newInstance(parameters);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }

        return null;
    }

    public <T> T getInstance(Class<T> classs) {
        @SuppressWarnings("unchecked")
        Function<ServiceRegistry, T> function = (Function<ServiceRegistry, T>) bindingMap.get(classs);
        if (function != null)
            return function.apply(this);
        return createInstance(classs);
    }
}
