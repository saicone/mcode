package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ArgumentType {

    // Brigadier types
    WORD(String.class),
    STRING(String.class),
    GREEDY_STRING(String.class), // Can be represented as array argument
    BOOLEAN(Boolean.class),
    INTEGER(Integer.class),
    FLOAT(Float.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    // Vanilla arguments (Taken from Paper API)
    // Range arguments not included, use INTEGER, FLOAT, LONG or DOUBLE instead, those will be automatically computed
    ENTITY,
    ENTITIES,
    PLAYER,
    PLAYER_PROFILES,
    PLAYERS,
    BLOCK_POSITION,
    BLOCK_STATE,
    ITEM_STACK,
    NAMED_COLOR,
    COMPONENT,
    SIGNED_MESSAGE,
    SCOREBOARD_DISPLAY_SLOT,
    NAMESPACED_KEY,
    WORLD,
    GAME_MODE,
    HEIGHT_MAP,
    UNIQUE_ID,
    // Custom arguments (Taken from CommandAPI library)
    AXIS,
    CHAT_COLOR,
    BIOME,
    ENCHANTMENT,
    LOOT_TABLE,
    PARTICLE,
    POTION_EFFECT,
    RECIPE,
    SOUND,
    TIME;

    public static final ArgumentType[] VALUES = values();

    private final Class<?> type;

    ArgumentType() {
        this(null);
    }

    ArgumentType(@Nullable Class<?> type) {
        this.type = type;
    }

    @Nullable
    public Class<?> getType() {
        return type;
    }

    @Nullable
    public static ArgumentType of(@NotNull Class<?> type) {
        for (ArgumentType value : VALUES) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }
}
