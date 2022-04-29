package com.github.writingbettercodethanyou.gamerpluginframework.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

        // bukkit classes
        put(World.class, (section, path) -> {
            String value = section.getString(path);
            try {
                return Bukkit.getWorld(UUID.fromString(value));
            } catch (IllegalArgumentException ignored) {
                return Bukkit.getWorld(value);
            }
        });
        put(Location.class, (section, path) ->
            new Location(
                    readValue(World.class, section, "world"),
                    readValue(double.class, section, "x"),
                    readValue(double.class, section, "y"),
                    readValue(double.class, section, "z"),
                    readValue(float.class, section, "yaw"),
                    readValue(float.class, section, "pitch"))
        );
    }};

    public ConfigReader(Class<T> configClass) {
        try {
            this.constructor = configClass.getConstructor();
        } catch (NoSuchMethodException exception) {
            throw new IllegalArgumentException("config class must have an empty constructor", exception);
        }
    }

    public void addReader(Class<?> targetClass, BiFunction<ConfigurationSection, String, Object> readFunction) {
        readerFunctions.put(targetClass, readFunction);
    }

    public <V> V readValue(Class<V> expectedClass, ConfigurationSection configuration, String path) {
        BiFunction<ConfigurationSection, String, Object> readerFunction = readerFunctions.get(expectedClass);
        V value;
        if(readerFunction != null) {
            value = (V) readerFunction.apply(configuration, path);
        } else {
            value = new ConfigReader<>(expectedClass).readConfig(configuration.getConfigurationSection(path));
        }
        return value;
    }

    public T readConfig(ConfigurationSection configuration) {
        T config;
        try {
            config = constructor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }

        for (Field field : config.getClass().getDeclaredFields()) {
            Object  value        = readValue(field.getType(), configuration, getConfigPath(field));
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
