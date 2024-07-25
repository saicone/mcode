package com.saicone.mcode;

import com.saicone.mcode.platform.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Platform {

    private static final UUID CONSOLE_ID = new UUID(0, 0);

    protected static Platform INSTANCE;
    protected static final Map<String, String> NAMES = new HashMap<>();

    static {
        // Extension
        NAMES.put("cache", "com.saicone.mcode.util.cache.Cache");
        NAMES.put("lookup", "com.saicone.mcode.util.EasyLookup");
        NAMES.put("type", "com.saicone.mcode.util.OptionalType");
        // Module
        NAMES.put("command", "com.saicone.mcode.module.command.CommandCentral");
        NAMES.put("delivery", "com.saicone.delivery4j.DeliveryClient");
        NAMES.put("lang", "com.saicone.mcode.module.lang.LangLoader");
        NAMES.put("script", "com.saicone.mcode.module.script.Script");
        NAMES.put("settings", "com.saicone.settings.Settings");
        NAMES.put("task", "com.saicone.mcode.scheduler.Task");
    }

    protected Platform() {
        if (INSTANCE == null) {
            initModules();
        }
    }

    protected void initModules() {
    }

    protected void initModule(@NotNull String name, @NotNull String... methods) {
        try {
            final Class<?> clazz = Class.forName(name, false, this.getClass().getClassLoader());
            for (String method : methods) {
                clazz.getDeclaredMethod(method).invoke(null);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T extends Platform> T get() {
        return (T) INSTANCE;
    }

    @NotNull
    public static Platform getInstance() {
        return INSTANCE;
    }

    protected static void setInstance(@NotNull Platform platform) {
        if (Platform.INSTANCE == null) {
            Platform.INSTANCE = platform;
        }
    }

    public static boolean isAvailable(@NotNull String name) {
        final String value = NAMES.get(name.toLowerCase());
        try {
            if (value != null) {
                Class.forName(value);
            } else {
                Class.forName(name);
            }
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @NotNull
    public UUID getUserId(@Nullable Object user) {
        return CONSOLE_ID;
    }

    @NotNull
    public Text getText(@NotNull String s) {
        return new Text(s);
    }

    @NotNull
    public abstract Collection<?> getOnlinePlayers();
}
