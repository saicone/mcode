package com.saicone.mcode.module.script.condition;

import com.saicone.mcode.module.script.Condition;
import com.saicone.mcode.module.script.EvalUser;
import com.saicone.mcode.module.script.ScriptFunction;
import com.saicone.mcode.module.script.action.Delay;
import com.saicone.mcode.util.CacheSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Cooldown extends Condition {

    private static final Map<String, CacheSet<String>> CACHES = new HashMap<>();

    static {
        CACHES.put("main", new CacheSet<>());
    }

    public Cooldown(@Nullable String value) {
        super(value);
    }

    @Override
    public @Nullable ScriptFunction<EvalUser, Boolean> build() {
        if (getValue() == null) {
            return null;
        }
        final String[] split = getValue().split(" ", 3);
        final TimeUnit unit = split.length > 1 ? Delay.parseUnit(split[1]) : TimeUnit.SECONDS;
        final long time = unit.toMillis(Delay.parseTime(split[0]));
        if (time < 1) {
            return null;
        }
        final String id;
        if (split[split.length - 1].contains(":")) {
            id = split[split.length - 1];
        } else {
            id = "main";
        }
        return (user) -> {
            final String userId = user.getId();
            if (userId == null) {
                return true;
            }
            return !getCacheOrCreate(id).containsOrAdd(user.getId(), time);
        };
    }

    @NotNull
    public static CacheSet<String> getCache(@NotNull String s) {
        return CACHES.getOrDefault(s, CACHES.get("main"));
    }

    @NotNull
    public static CacheSet<String> getCacheOrCreate(@NotNull String s) {
        CacheSet<String> cache = CACHES.get(s);
        if (cache == null) {
            if (s.contains(":")) {
                cache = new CacheSet<>(10000, 1000);
                CACHES.put(s, cache);
            } else {
                cache = CACHES.get("main");
            }
        }
        return cache;
    }
}
