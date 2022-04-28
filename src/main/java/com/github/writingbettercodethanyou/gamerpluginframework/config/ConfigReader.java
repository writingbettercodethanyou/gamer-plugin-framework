package com.github.writingbettercodethanyou.gamerpluginframework.config;

import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class ConfigReader<T> {

    private final Constructor<T> constructor;

    private final Map<Class<?>, BiFunction<ConfigurationSection, String, Object>> readerFunctions = new HashMap<>()
    {{
        // primitives
        put(boolean.class, ConfigurationSection::getBoolean);
        put(byte.class, (section, path) -> Integer.valueOf(section.getInt(path)).byteValue());
        put(short.class, (section, path) -> Integer.valueOf(section.getInt(path)).shortValue());
        put(int.class, ConfigurationSection::getInt);
        put(long.class, ConfigurationSection::getLong);
        put(float.class, (section, path) -> Double.valueOf(section.getDouble(path)).floatValue());
        put(double.class, ConfigurationSection::getDouble);

        // primitive wrappers
        put(Boolean.class, ConfigurationSection::getBoolean);
        put(Byte.class, (section, path) -> Integer.valueOf(section.getInt(path)).byteValue());
        put(Short.class, (section, path) -> Integer.valueOf(section.getInt(path)).shortValue());
        put(Integer.class, ConfigurationSection::getInt);
        put(Long.class, ConfigurationSection::getLong);
        put(Float.class, (section, path) -> Double.valueOf(section.getDouble(path)).floatValue());
        put(Double.class, ConfigurationSection::getDouble);

        // non-primitive classes
        put(String.class, ConfigurationSection::getString);
    }};

    public ConfigReader(Class<T> configClass) {
        try {
            this.constructor = configClass.getConstructor();
        } catch (NoSuchMethodException exception) {
            throw new IllegalArgumentException("config class must have an empty constructor", exception);
        }
    }

    public T readConfig(ConfigurationSection configuration) {
        T config;
        try {
            config = constructor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }

        for (Field field : config.getClass().getDeclaredFields()) {
            BiFunction<ConfigurationSection, String, Object> readerFunction = readerFunctions.get(field.getType());
            Object value;
            if(readerFunction != null) {
                value = readerFunction.apply(configuration, getConfigPath(field));
            } else {
                value = new ConfigReader<>(field.getType()).readConfig(configuration.getConfigurationSection(getConfigPath(field)));
            }

            boolean changeAccess = !field.canAccess(config);
            if (changeAccess)
                field.setAccessible(true);
            try {
                field.set(config, value);
            } catch (IllegalAccessException ignored) {
            } finally {
                if (changeAccess)
                    field.setAccessible(false);
            }
        }

        return config;
    }

    private static String getConfigPath(Field field) {
        ConfigPath pathAnnotation = field.getAnnotation(ConfigPath.class);
        if (pathAnnotation == null)
            return field.getName();
        return pathAnnotation.value();
    }
}
