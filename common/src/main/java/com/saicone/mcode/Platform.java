package com.saicone.mcode;

import com.saicone.mcode.platform.Text;
import org.jetbrains.annotations.NotNull;

public abstract class Platform {

    private static Platform instance;

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T extends Platform> T get() {
        return (T) instance;
    }

    @NotNull
    public static Platform getInstance() {
        return instance;
    }

    protected static void setInstance(@NotNull Platform platform) {
        if (Platform.instance == null) {
            Platform.instance = platform;
        }
    }

    @NotNull
    public Text getText(@NotNull String s) {
        return new Text(s);
    }
}
