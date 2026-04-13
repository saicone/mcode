package com.saicone.mcode.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class JarIO implements AutoCloseable {

    @NotNull
    public static JarIO valueOf(@NotNull Class<?> clazz) throws IOException {
        return valueOf(clazz.getProtectionDomain().getCodeSource().getLocation());
    }

    @NotNull
    public static JarIO valueOf(@NotNull URL url) throws IOException {
        File file;
        try {
            try {
                file = new File(url.toURI());
            } catch (IllegalArgumentException e) {
                file = new File(((JarURLConnection) url.openConnection()).getJarFileURL().toURI());
            }
        } catch (URISyntaxException e) {
            file = new File(url.getPath());
        }
        return new JarIO(new JarFile(file));
    }

    private final JarFile jar;

    public JarIO(@NotNull JarFile jar) {
        this.jar = jar;
    }

    @Override
    public void close() throws IOException {
        jar.close();
    }

    public void saveResources(@NotNull File folder, @NotNull Predicate<JarEntry> predicate) throws IOException {
        saveResources(folder, false, predicate);
    }

    public void saveResources(@NotNull File folder, boolean replace, @NotNull Predicate<JarEntry> predicate) throws IOException {
        try (Stream<JarEntry> stream = jar.stream()) {
            final Iterator<JarEntry> entries = stream.filter(predicate).iterator();
            while (entries.hasNext()) {
                final JarEntry entry = entries.next();
                final File file = new File(folder, entry.getName());
                if (file.exists() && !replace) {
                    continue;
                }
                file.getParentFile().mkdirs();
                try (InputStream in = jar.getInputStream(entry)) {
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
