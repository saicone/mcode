package com.saicone.mcode.platform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public enum MinecraftVersion {

    V_1_7(null, 3, 1, null, 1),
    V_1_7_1(null, 3, 1, null, 1),
    V_1_7_2(null, 4, 1, null, 1),
    V_1_7_3(null, 4, 1, null, 2),
    V_1_7_4(null, 4, 1, null, 2),
    V_1_7_5(null, 4, 1, null, 2),
    V_1_7_6(null, 5, 1, null, 3),
    V_1_7_7(null, 5, 1, null, 3),
    V_1_7_8(null, 5, 1, null, 3),
    V_1_7_9(null, 5, 1, null, 3),
    V_1_7_10(null, 5, 1, null, 4),

    V_1_8(null, 47, 1, null, 1),
    V_1_8_1(null, 47, 1, null, 1),
    V_1_8_2(null, 47, 1, null, 1),
    V_1_8_3(null, 47, 1, null, 2),
    V_1_8_4(null, 47, 1, null, 2),
    V_1_8_5(null, 47, 1, null, 2),
    V_1_8_6(null, 47, 1, null, 2),
    V_1_8_7(null, 47, 1, null, 2),
    V_1_8_8(null, 47, 1, null, 3),
    V_1_8_9(null, 47, 1, null, 3),

    V_1_9(169, 107, 2, null, 1),
    V_1_9_1(175, 108, 2, null, 1),
    V_1_9_2(176, 109, 2, null, 1),
    V_1_9_3(183, 110, 2, null, 2),
    V_1_9_4(184, 110, 2, null, 2),

    V_1_10(510, 210, 2, null, 1),
    V_1_10_1(511, 210, 2, null, 1),
    V_1_10_2(512, 210, 2, null, 1),

    V_1_11(819, 315, 3, null, 1),
    V_1_11_1(921, 316, 3, null, 1),
    V_1_11_2(922, 316, 3, null, 1),

    V_1_12(1139, 335, 3, null, 1),
    V_1_12_1(1241, 338, 3, 3, 1),
    V_1_12_2(1343, 340, 3, 3, 1),

    V_1_13(1519, 393, 4, 4, 1),
    V_1_13_1(1628, 401, 4, 4, 2),
    V_1_13_2(1631, 404, 4, 4, 2),

    V_1_14(1952, 477, 4, 4, 1),
    V_1_14_1(1957, 480, 4, 4, 1),
    V_1_14_2(1963, 485, 4, 4, 1),
    V_1_14_3(1968, 490, 4, 4, 1),
    V_1_14_4(1976, 498, 4, 4, 1),

    V_1_15(2225, 573, 5, 5, 1),
    V_1_15_1(2227, 575, 5, 5, 1),
    V_1_15_2(2230, 578, 5, 5, 1),

    V_1_16(2566, 735, 5, 5, 1),
    V_1_16_1(2567, 736, 5, 5, 1),
    V_1_16_2(2578, 751, 6, 6, 2),
    V_1_16_3(2580, 753, 6, 6, 2),
    V_1_16_4(2584, 754, 6, 6, 3),
    V_1_16_5(2586, 754, 6, 6, 3),

    V_1_17(2724, 755, 7, 7, 1),
    V_1_17_1(2730, 756, 7, 7, 1),

    V_1_18(2860, 757, 8, 8, 1),
    V_1_18_1(2866, 757, 8, 8, 1),
    V_1_18_2(2975, 758, 8, 9, 2),

    V_1_19(3105, 759, 9, 10, 1),
    V_1_19_1(3117, 760, 9, 10, 1),
    V_1_19_2(3120, 760, 9, 10, 1),
    V_1_19_3(3218, 761, 12, 10, 2),
    V_1_19_4(3337, 762, 13, 12, 3),

    V_1_20(3463, 763, 15, 15, 1),
    V_1_20_1(3465, 763, 15, 15, 1),
    V_1_20_2(3578, 764, 18, 18, 2),
    V_1_20_3(3698, 765, 22, 26, 3),
    V_1_20_4(3700, 765, 22, 26, 3),
    V_1_20_5(3837, 766, 32, 41, 4),
    V_1_20_6(3839, 766, 32, 41, 4),

    V_1_21(3953, 767, 34, 48, 1),
    V_1_21_1(3955, 767, 34, 48, 1);

    public static final MinecraftVersion[] VALUES = values();
    public static MinecraftVersion SERVER = VALUES[VALUES.length - 1];

    private final Integer dataVersion;
    private final int protocol;
    private final int resourcePackFormat;
    private final Integer dataPackFormat;
    private final int revision;

    private final int major;
    private final int feature;
    private final int minor;

    private final float floatVersion;
    private final int fullVersion;
    private final String bukkitPackage;

    MinecraftVersion(@Nullable Integer dataVersion, int protocol, int resourcePackFormat, @Nullable Integer dataPackFormat, int revision) {
        this.dataVersion = dataVersion;
        this.protocol = protocol;
        this.resourcePackFormat = resourcePackFormat;
        this.dataPackFormat = dataPackFormat;
        this.revision = revision;

        final String[] split = this.name().split("_");

        this.major = Integer.parseInt(split[1]);
        this.feature = Integer.parseInt(split[2]);
        this.minor = split.length > 3 ? Integer.parseInt(split[3]) : 0;

        final String featureFormatted = feature >= 10 ? String.valueOf(feature) : "0" + feature;
        final String minorFormatted = minor >= 10 ? String.valueOf(minor) : "0" + minor;

        this.floatVersion = Float.parseFloat(feature + "." + minorFormatted);
        this.fullVersion = Integer.parseInt(split[1] + featureFormatted + minorFormatted);
        this.bukkitPackage = "v" + split[1] + "_" + feature + "_R" + revision;
    }

    public boolean isLegacy() {
        return major <= 12;
    }

    public boolean isFlat() {
        return major >= 13;
    }

    public boolean isUniversal() {
        return major >= 17;
    }

    public boolean isDataComponent() {
        return dataVersion >= 3837;
    }

    public boolean isOlderThan(@NotNull MinecraftVersion version) {
        return this.ordinal() < version.ordinal();
    }

    public boolean isOlderThanOrEquals(@NotNull MinecraftVersion version) {
        return this.ordinal() <= version.ordinal();
    }

    public boolean isNewerThan(@NotNull MinecraftVersion version) {
        return this.ordinal() > version.ordinal();
    }

    public boolean isNewerThanOrEquals(@NotNull MinecraftVersion version) {
        return this.ordinal() >= version.ordinal();
    }

    @Nullable
    public Integer dataVersion() {
        return dataVersion;
    }

    public int protocol() {
        return protocol;
    }

    public int resourcePackFormat() {
        return resourcePackFormat;
    }

    @Nullable
    public Integer dataPackFormat() {
        return dataPackFormat;
    }

    public int revision() {
        return revision;
    }

    public int major() {
        return major;
    }

    public int feature() {
        return feature;
    }

    public int minor() {
        return minor;
    }

    public float getFloatVersion() {
        return floatVersion;
    }

    public int getFullVersion() {
        return fullVersion;
    }

    @NotNull
    public String getBukkitPackage() {
        return bukkitPackage;
    }

    @Nullable
    public static MinecraftVersion fromString(@NotNull String s) {
        final String[] split = s.split("\\.");
        if (split.length < 2) {
            return null;
        }
        final int major = Integer.parseInt(split[0]);
        final int feature;
        final int minor;
        if (split[1].contains("-") || split[1].contains("_")) {
            feature = Integer.parseInt(split[1].split("[-_]")[0]);
            minor = 0;
        } else {
            feature = Integer.parseInt(split[1]);
            minor = split.length > 2 ? Integer.parseInt(split[2].split("[-_]")[0]) : 0;
        }
        for (MinecraftVersion value : VALUES) {
            if (value.major == major && value.feature == feature && value.minor == minor) {
                return value;
            }
        }
        return null;
    }
    
    @Nullable
    public static <T> MinecraftVersion from(@NotNull Function<@NotNull MinecraftVersion, @Nullable T> valueFunction, @Nullable T t) {
        final boolean isNumber = t instanceof Number;
        for (int i = VALUES.length; i-- > 0; ) {
            final MinecraftVersion value = VALUES[i];
            final T obj = valueFunction.apply(value);
            if (isNumber && obj instanceof Number) {
                if (((Number) obj).doubleValue() >= ((Number) t).doubleValue()) {
                    return value;
                }
            } else if (Objects.equals(t, valueFunction.apply(value))) {
                return value;
            }
        }
        return null;
    }
}