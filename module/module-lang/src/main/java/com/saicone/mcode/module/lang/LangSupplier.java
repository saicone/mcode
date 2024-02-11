package com.saicone.mcode.module.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

@FunctionalInterface
public interface LangSupplier {

    String DEFAULT_LANGUAGE = "en_us";
    Set<String> DEFAULT_LANGUAGE_TYPES = Set.of("en_us");
    Map<String, String> DEFAULT_LANGUAGE_ALIASES = Map.of(
            "en_au", "en_us",
            "en_ca", "en_us",
            "en_gb", "en_us",
            "en_nz", "en_us",
            "es_ar", "es_es",
            "es_cl", "es_es",
            "es_ec", "es_es",
            "es_mx", "es_es",
            "es_uy", "es_es",
            "es_ve", "es_es"
    );
    int DEFAULT_LOG_LEVEL = 2;

    @NotNull
    String getLanguage();

    @NotNull
    default String getLanguageFor(@Nullable Object object) {
        return getLanguage();
    }

    @NotNull
    default Set<String> getLanguageTypes() {
        return DEFAULT_LANGUAGE_TYPES;
    }

    @NotNull
    default Map<String, String> getLanguageAliases() {
        return DEFAULT_LANGUAGE_ALIASES;
    }

    @NotNull
    default String getEffectiveLanguage(@NotNull Object language) {
        final String s = (language instanceof String ? (String) language : getLanguageFor(language)).toLowerCase();
        if (getLanguageTypes().contains(s)) {
            return s;
        }
        return getLanguageAliases().getOrDefault(s, getLanguage());
    }

    default int getLogLevel() {
        return DEFAULT_LOG_LEVEL;
    }
}
