package com.saicone.mcode;

import com.saicone.mcode.platform.PlatformType;
import com.saicone.mcode.platform.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public abstract class Platform {

    private static final UUID CONSOLE_ID = new UUID(0, 0);
    protected static Platform INSTANCE;

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T extends Platform> T get() {
        return (T) INSTANCE;
    }

    @NotNull
    public static Platform getInstance() {
        return INSTANCE;
    }

    private final PlatformType type;

    protected Platform(@NotNull PlatformType type) {
        if (INSTANCE != null) {
            throw new IllegalStateException("The platform instance is already initialized");
        }
        INSTANCE = this;
        this.type = type;
    }

    @NotNull
    public PlatformType getType() {
        return type;
    }

    @NotNull
    public UUID getUserId(@Nullable Object user) {
        return CONSOLE_ID;
    }

    @NotNull
    public abstract Text getText(byte type, @NotNull Object object);

    @NotNull
    public abstract Collection<?> getOnlinePlayers();
}
