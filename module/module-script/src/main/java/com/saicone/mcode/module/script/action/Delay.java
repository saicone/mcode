package com.saicone.mcode.module.script.action;

import com.saicone.mcode.module.script.Action;
import com.saicone.mcode.module.script.ActionResult;
import com.saicone.mcode.module.script.EvalUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class Delay extends Action {

    public static final Builder<Delay> BUILDER = new Builder<Delay>("(?i)delay|wait")
            .map(map -> {
                final long time = map.getBy(Delay::parseTime, m -> m.getIgnoreCase("time"), 0L);
                final TimeUnit unit = map.getBy(Delay::parseUnit, m -> m.getRegex("(?i)(time-?)?unit"), TimeUnit.SECONDS);
                return new Delay(time, unit);
            })
            .list(list -> {
                final long time = list.size() > 0 ? parseTime(list.get(0)) : 0L;
                final TimeUnit unit = list.size() > 1 ? parseUnit(list.get(1)) : TimeUnit.SECONDS;
                return new Delay(time, unit);
            })
            .text(s -> {
                final String[] split = s.split(" ", 2);
                final long time = split.length > 0 ? parseTime(split[0]) : 0L;
                final TimeUnit unit = split.length > 1 ? parseUnit(split[1]) : TimeUnit.SECONDS;
                return new Delay(time, unit);
            });

    private final long time;
    private final TimeUnit unit;

    public Delay(long time, @NotNull TimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }

    public long getTime() {
        return time;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    @Override
    public @NotNull ActionResult run(@NotNull EvalUser user) {
        if (time > 0) {
            return ActionResult.DONE.delay(time, unit);
        }
        return ActionResult.DONE;
    }

    public static long parseTime(@Nullable Object o) {
        if (o instanceof Long) {
            return (Long) o;
        }
        try {
            return Long.parseLong(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @NotNull
    public static TimeUnit parseUnit(@Nullable Object o) {
        if (o instanceof TimeUnit) {
            return (TimeUnit) o;
        }
        try {
            return TimeUnit.valueOf(String.valueOf(o).trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TimeUnit.SECONDS;
        }
    }
}
