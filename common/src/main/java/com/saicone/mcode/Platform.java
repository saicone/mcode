package com.saicone.mcode;

import com.saicone.mcode.platform.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Platform {

    private static Platform INSTANCE;
    private static final Map<String, String> NAMES = new HashMap<>();

    static {
        // Extension
        NAMES.put("cacheset", "com.saicone.mcode.util.CacheSet");
        NAMES.put("lookup", "com.saicone.mcode.util.EasyLookup");
        NAMES.put("type", "com.saicone.mcode.util.OptionalType");
        // Module
        NAMES.put("delivery", "com.saicone.mcode.delivery.DeliveryClient");
        NAMES.put("lang", "com.saicone.mcode.lang.LangLoader");
        NAMES.put("script", "com.saicone.mcode.script.Script");
        NAMES.put("settings", "com.saicone.mcode.settings.Settings");
        NAMES.put("task", "com.saicone.mcode.scheduler.Task");
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

    @Nullable
    public String getUserId(@Nullable Object user) {
        return null;
    }

    @NotNull
    public Text getText(@NotNull String s) {
        return new Text(s);
    }

    @NotNull
    public abstract Collection<?> getOnlinePlayers();
}
