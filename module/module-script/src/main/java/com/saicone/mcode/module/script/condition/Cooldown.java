package com.saicone.mcode.module.script.condition;

import com.saicone.mcode.module.script.Condition;
import com.saicone.mcode.module.script.EvalUser;
import com.saicone.mcode.module.script.ScriptFunction;
import com.saicone.mcode.module.script.action.Delay;
import com.saicone.mcode.util.cache.Cache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Cooldown extends Condition {

    private static final Map<String, Set<String>> CACHES = new HashMap<>();

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
        final long duration = Delay.parseTime(split[0]);
        if (duration < 1) {
            return null;
        }
        return (user) -> {
            final String userId = user.getId();
            if (userId == null) {
                return true;
            }
            final Set<String> cache = getCacheOrCreate(duration, unit);
            if (cache.contains(userId)) {
                return false;
            } else {
                cache.add(userId);
                return true;
            }
        };
    }

    @Nullable
    public static Set<String> getCache(long duration, @NotNull TimeUnit unit) {
        return CACHES.get(String.valueOf(unit.toMillis(duration)));
    }

    @NotNull
    public static Set<String> getCacheOrCreate(long duration, @NotNull TimeUnit unit) {
        final String key = String.valueOf(unit.toMillis(duration));
        Set<String> cache = CACHES.get(key);
        if (cache == null) {
            cache = Collections.newSetFromMap(Cache.<String, Boolean>newBuilder().expireAfterWrite(duration, unit).build().asMap());
            CACHES.put(key, cache);
        }
        return cache;
    }
}
