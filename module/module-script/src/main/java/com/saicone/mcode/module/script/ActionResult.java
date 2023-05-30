package com.saicone.mcode.module.script;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ActionResult {

    public static ActionResult DONE = new ActionResult("DONE");
    public static ActionResult RETURN = new ActionResult("RETURN");
    public static ActionResult BREAK = new ActionResult("BREAK");
    public static ActionResult CONTINUE = new ActionResult("CONTINUE");

    private final String id;
    private final boolean main;

    private long delay;
    private TimeUnit timeUnit;
    private Object data;

    public static ActionResult of(@NotNull String id) {
        switch (id.toUpperCase()) {
            case "DONE":
                return DONE;
            case "RETURN":
                return RETURN;
            case "BREAK":
            case "STOP":
                return BREAK;
            case "CONTINUE":
                return CONTINUE;
            default:
                return new ActionResult(id, false);
        }
    }

    ActionResult(@NotNull String id) {
        this(id, true);
    }

    ActionResult(@NotNull String id, boolean main) {
        this.id = id;
        this.main = main;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public long getDelay() {
        return delay;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public Object getData() {
        return data;
    }

    public boolean hasDelay() {
        return timeUnit != null;
    }

    public boolean hasData() {
        return data != null;
    }

    @NotNull
    public ActionResult delay(long delay, @NotNull TimeUnit timeUnit) {
        if (main) {
            return new ActionResult(this.id, false).delay(delay, timeUnit);
        }
        this.delay = delay;
        this.timeUnit = timeUnit;
        return this;
    }

    @NotNull
    public ActionResult data(Object data) {
        if (main) {
            return new ActionResult(this.id, false).data(data);
        }
        this.data = data;
        return this;
    }

    @NotNull
    public ActionResult transfer(@NotNull String id) {
        if (main) {
            return of(id);
        }
        final ActionResult result = new ActionResult(id, false);
        result.delay = this.delay;
        result.timeUnit = this.timeUnit;
        result.data = this.data;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionResult that = (ActionResult) o;

        return id.equalsIgnoreCase(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
