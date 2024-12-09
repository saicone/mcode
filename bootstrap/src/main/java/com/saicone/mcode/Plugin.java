package com.saicone.mcode;

import com.saicone.mcode.bootstrap.Bootstrap;
import com.saicone.mcode.env.Env;
import com.saicone.mcode.env.Executes;
import com.saicone.mcode.util.text.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Supplier;

public class Plugin {

    private static Bootstrap BOOTSTRAP;
    private static Plugin INSTANCE;

    protected int logLevel = 3;

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T extends Bootstrap> T bootstrap() {
        return (T) BOOTSTRAP;
    }

    public static void log(int level, @NotNull Supplier<String> msg) {
        if (level > INSTANCE.logLevel) {
            return;
        }
        BOOTSTRAP.log(level, msg);
    }

    public static void log(int level, @NotNull String msg, @Nullable Object... args) {
        if (level > INSTANCE.logLevel) {
            return;
        }
        BOOTSTRAP.log(level, () -> Strings.replaceArgs(msg, args));
    }

    public static void logException(int level, @NotNull Throwable throwable) {
        if (level > INSTANCE.logLevel) {
            return;
        }
        BOOTSTRAP.logException(level, throwable);
    }

    public static void logException(int level, @NotNull Throwable throwable, @NotNull Supplier<String> msg) {
        if (level > INSTANCE.logLevel) {
            return;
        }
        BOOTSTRAP.logException(level, throwable, msg);
    }

    public static void logException(int level, @NotNull Throwable throwable, @NotNull String msg, @Nullable Object... args) {
        if (level > INSTANCE.logLevel) {
            return;
        }
        BOOTSTRAP.logException(level, throwable, () -> Strings.replaceArgs(msg, args));
    }

    public static void reload() {
        Env.execute(Executes.RELOAD, true);
        INSTANCE.onReload();
        Env.execute(Executes.RELOAD, false);
    }

    public Plugin() {
        if (INSTANCE != null) {
            throw new IllegalStateException("The plugin instance is already initialized");
        }
        INSTANCE = this;
    }

    @NotNull
    public Path getFolder() {
        return BOOTSTRAP.folder();
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public void onLoad() {
        // empty default method
    }

    public void onEnable() {
        // empty default method
    }

    public void onDisable() {
        // empty default method
    }

    protected void onReload() {
        // empty default method
    }
}
