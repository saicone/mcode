package com.saicone.mcode;

import com.saicone.mcode.bootstrap.Bootstrap;
import com.saicone.mcode.env.Env;
import com.saicone.mcode.env.Executes;
import com.saicone.mcode.util.logging.LogFilter;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class Plugin {

    private static Bootstrap BOOTSTRAP;
    private static Plugin INSTANCE;

    protected LogFilter logger;
    protected int logLevel = 3;

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T extends Bootstrap> T bootstrap() {
        return (T) BOOTSTRAP;
    }

    @NotNull
    public static LogFilter logger() {
        return INSTANCE.logger;
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

        this.logger = LogFilter.valueOf(BOOTSTRAP.logger(), () -> this.logLevel);
    }

    @NotNull
    public Path getFolder() {
        return BOOTSTRAP.folder();
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <L> L getLogger() {
        return (L) BOOTSTRAP.logger();
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(@MagicConstant(valuesFromClass = LogFilter.class) int logLevel) {
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
