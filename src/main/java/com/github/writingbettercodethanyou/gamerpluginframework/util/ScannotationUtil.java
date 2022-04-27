package com.github.writingbettercodethanyou.gamerpluginframework.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ScannotationUtil {

    public static Set<Class<?>> findClassesWithAnnotation(Class<?> sourceClass, Class<? extends Annotation> annotationClass) {
        Set<Class<?>> matchedClasses = new HashSet<>();

        File file;
        try {
            file = new File(sourceClass.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException exception) {
            throw new RuntimeException(exception);
        }

        try (ZipFile zip = new ZipFile(file)) {
            for (Enumeration<? extends ZipEntry> list = zip.entries(); list.hasMoreElements(); ) {
                ZipEntry entry = list.nextElement();
                String name = entry.getName();

                if (!name.endsWith(".class"))
                    continue;

                Class<?> classs = Class.forName(name.substring(0, name.lastIndexOf(".")).replace("/", "."));
                Annotation annotation = classs.getAnnotation(annotationClass);
                if (annotation == null) {
                    continue;
                }

                matchedClasses.add(classs);
            }
        } catch (IOException | ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }

        return matchedClasses;
    }

    private ScannotationUtil() {
        throw new UnsupportedOperationException("instantiate a utility class");
    }
}
