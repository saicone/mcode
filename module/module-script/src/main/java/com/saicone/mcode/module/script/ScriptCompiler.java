package com.saicone.mcode.module.script;

import com.saicone.mcode.module.script.action.Delay;
import com.saicone.mcode.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ScriptCompiler extends ScriptRegistry {

    private static final ScriptCompiler INSTANCE = new ScriptCompiler();

    @NotNull
    public static ScriptCompiler compiler() {
        return INSTANCE;
    }

    @Nullable
    public ScriptFunction<EvalUser, ActionResult> compileAction(@Nullable Object action) {
        return compileAction(action, null);
    }

    @Nullable
    public ScriptFunction<EvalUser, ActionResult> compileAction(@Nullable Object action, @Nullable Object object) {
        ScriptFunction<EvalUser, ActionResult> function = null;
        if (action instanceof Map) {
            ScriptFunction<EvalUser, ActionResult> run = null;
            ScriptFunction<EvalUser, Boolean> condition = null;
            ScriptFunction<EvalUser, ActionResult> orElse = null;
            for (var entry : ((Map<?, ?>) action).entrySet()) {
                final String key = String.valueOf(entry.getKey());
                switch (key.replace("-", "").replace(" ", "").toLowerCase()) {
                    case "if":
                    case "condition":
                    case "conditions":
                        condition = compileCondition(entry.getValue());
                        break;
                    case "run":
                    case "action":
                    case "actions":
                        run = compileAction(entry.getValue(), object);
                        break;
                    case "else":
                    case "orelse":
                    case "elserun":
                    case "orelserun":
                        orElse = compileAction(entry.getValue(), object);
                        break;
                    default:
                        function = concat(function, getAction(key, entry.getValue(), object));
                        break;
                }
            }
            if (condition != null) {
                if (orElse != null) {
                    if (run == null) {
                        run = (user) -> ActionResult.DONE;
                    }
                    run = run.ifAnd(condition, ActionResult.CONTINUE, orElse);
                } else if (run != null) {
                    run = run.ifElse(condition, null);
                }
            }

            function = concat(function, run);
        } else if (action instanceof Iterable) {
            final List<ScriptFunction<EvalUser, ActionResult>> actions = new ArrayList<>();
            for (Object o : (Iterable<?>) action) {
                final ScriptFunction<EvalUser, ActionResult> act = compileAction(o, object);
                if (act != null) {
                    actions.add(act);
                }
            }
            if (!actions.isEmpty()) {
                function = (user) -> run(user, actions, 0);
            }
        } else if (action instanceof Object[]) {
            function = getAction(asString((Object[]) action), object);
        } else {
            function = getAction(String.valueOf(action), object);
        }
        return function;
    }

    @Nullable
    public ScriptFunction<EvalUser, Boolean> compileCondition(@Nullable Object condition) {
        return compileCondition(condition, null);
    }

    @Nullable
    public ScriptFunction<EvalUser, Boolean> compileCondition(@Nullable Object condition, @Nullable Object object) {
        final List<ScriptFunction<EvalUser, Boolean>> conditions = getConditions(condition, object);
        if (conditions.isEmpty()) {
            return null;
        }
        return (user) -> {
            for (ScriptFunction<EvalUser, Boolean> function : conditions) {
                if (!function.apply(user)) {
                    return false;
                }
            }
            return true;
        };
    }

    @Override
    public boolean containsAnyAction(@NotNull Object key) {
        return super.containsAnyAction(key) || Script.REGISTRY.containsAnyAction(key);
    }

    @Override
    public boolean containsAnyCondition(@NotNull Object key) {
        return super.containsAnyCondition(key) || Script.REGISTRY.containsAnyCondition(key);
    }

    @Override
    public @Nullable EvalBuilder<? extends Action> getAction(@NotNull Object key) {
        var result = super.getAction(key);
        return result != null ? result : Script.REGISTRY.getAction(key);
    }

    @Override
    public @Nullable EvalBuilder<? extends Condition> getCondition(@NotNull Object key) {
        var result = super.getCondition(key);
        return result != null ? result : Script.REGISTRY.getCondition(key);
    }

    @Override
    public @Nullable ScriptFunction<EvalUser, ActionResult> getCompiledAction(@NotNull Object key) {
        var result = super.getCompiledAction(key);
        return result != null ? result : Script.REGISTRY.getCompiledAction(key);
    }

    @Override
    public @Nullable ScriptFunction<EvalUser, Boolean> getCompiledCondition(@NotNull Object key) {
        var result = super.getCompiledCondition(key);
        return result != null ? result : Script.REGISTRY.getCompiledCondition(key);
    }

    @Nullable
    public ScriptFunction<EvalUser, ActionResult> getAction(@NotNull String id, @Nullable Object object) {
        final String[] split = id.split("[:=;*'\"]", 2);
        if (split.length < 2) {
            return getAction(id.trim(), null, object);
        } else {
            final char start = id.charAt(split[0].length());
            final char end = split[1].charAt(split[1].length() - 1);
            if (start == end && (start == '\'' || start == '"')) {
                split[1] = split[1].substring(0, split[1].length() - 1);
            }
            return getAction(split[0].trim(), split[1].startsWith(" ") ? split[1].substring(1) : split[1], object);
        }
    }

    @Nullable
    public ScriptFunction<EvalUser, ActionResult> getAction(@NotNull String id, @Nullable Object value, @Nullable Object object) {
        String finalId = id.replace("-", "").replace(" ", "").toLowerCase();
        ScriptFunction<EvalUser, ActionResult> found = null;
        switch (finalId) {
            case "done":
                found = Action.DONE;
                break;
            case "return":
                return Action.RETURN;
            case "break":
            case "stop":
                found = Action.BREAK;
                break;
            case "continue":
                return Action.CONTINUE;
            default:
                break;
        }
        if (found != null) {
            final Delay delay = Delay.BUILDER.build(value);
            if (delay != null && delay.getTime() > 0) {
                return found.map(result -> result.delay(delay.getTime(), delay.getUnit()));
            }
            return found;
        }
        final EvalBuilder<? extends Action> builder = getAction(finalId);
        if (builder != null) {
            final Action action = builder.build(value);
            if (action != null) {
                return action.compile(object);
            }
        } else {
            return getCompiledAction(finalId);
        }
        return null;
    }

    @NotNull
    public List<ScriptFunction<EvalUser, Boolean>> getConditions(@Nullable Object condition, @Nullable Object object) {
        final List<ScriptFunction<EvalUser, Boolean>> conditions = new ArrayList<>();
        if (condition instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) condition).entrySet()) {
                if (entry.getValue() instanceof Iterable) {
                    final String id = String.valueOf(entry.getKey());
                    for (Object o : (Iterable<?>) entry.getValue()) {
                        conditions.add(getCondition(id, String.valueOf(o), object));
                    }
                } else {
                    conditions.add(getCondition(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()), object));
                }
            }
        } else if (condition instanceof Iterable) {
            for (Object o : (Iterable<?>) condition) {
                conditions.addAll(getConditions(o, object));
            }
        } else if (condition instanceof Object[]) {
            conditions.add(getCondition(asString((Object[]) condition), object));
        } else {
            conditions.add(getCondition(String.valueOf(condition), object));
        }
        conditions.removeIf(Objects::isNull);
        return conditions;
    }

    @Nullable
    public ScriptFunction<EvalUser, Boolean> getCondition(@NotNull String id, @Nullable Object object) {
        final String[] split = id.split("[:=;*]", 2);
        if (split.length < 2) {
            return getCondition(id.trim(), null, object);
        } else {
            return getCondition(split[0].trim(), split[1].startsWith(" ") ? split[1].substring(1) : split[1], object);
        }
    }

    @Nullable
    public ScriptFunction<EvalUser, Boolean> getCondition(@NotNull String id, @Nullable String value, @Nullable Object object) {
        String finalId = id.replace("-", "").replace(" ", "").toLowerCase();
        switch (finalId) {
            case "true":
            case "yes":
            case "y":
            case "1":
                return Condition.TRUE;
            case "false":
            case "no":
            case "n":
            case "0":
                return Condition.FALSE;
            default:
                break;
        }
        final boolean negative;
        if (finalId.startsWith("not")) {
            negative = true;
            finalId = finalId.substring(3);
        } else if (finalId.startsWith("!")) {
            negative = true;
            finalId = finalId.substring(1);
        } else {
            negative = false;
        }

        if (!containsAnyCondition(finalId)) {
            finalId = Strings.replacePrefix(finalId, "", "has", "are", "is", "meet");
        }

        final ScriptFunction<EvalUser, Boolean> function;
        final EvalBuilder<? extends Condition> builder = getCondition(finalId);
        if (builder != null) {
            final Condition condition = builder.build(value);
            if (condition != null) {
                function = condition.compile(object);
            } else {
                function = null;
            }
        } else {
            function = getCompiledCondition(finalId);
        }
        if (function == null) {
            return null;
        }
        if (negative) {
            return (user) -> !function.apply(user);
        }
        return function;
    }

    @Nullable
    protected ScriptFunction<EvalUser, ActionResult> concat(@Nullable ScriptFunction<EvalUser, ActionResult> base, @Nullable ScriptFunction<EvalUser, ActionResult> other) {
        if (other == null) {
            return base;
        }
        if (base == null) {
            return other;
        }
        return base.ifAnd(ActionResult.DONE, other);
    }

    @NotNull
    protected String asString(@NotNull Object[] objects) {
        final StringBuilder builder = new StringBuilder();
        for (Object o : objects) {
            builder.append(o);
        }
        return builder.toString();
    }

    @NotNull
    protected ActionResult run(EvalUser user, @NotNull List<ScriptFunction<EvalUser, ActionResult>> actions, int start) {
        for (int i = start; i < actions.size(); i++) {
            final var act = actions.get(i);
            final ActionResult result = act.apply(user);
            if (result == ActionResult.BREAK) {
                if (result.hasDelay()) {
                    return result.transfer("DONE");
                }
                break;
            } else if (result != ActionResult.DONE) {
                return result;
            } else if (result.hasDelay()) {
                if (i + 1 < actions.size()) {
                    final int newStart = i + 1;
                    run(result.getDelay(), result.getTimeUnit(), () -> run(user, actions, newStart));
                }
                return result;
            }
        }
        return ActionResult.DONE;
    }

    protected void run(long delay, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        // Dirty delay, must be overridden
        new Thread(() -> {
            try {
                Thread.sleep(unit.toMillis(delay));
                runnable.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
