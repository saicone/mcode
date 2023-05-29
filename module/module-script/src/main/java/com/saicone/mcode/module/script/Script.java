package com.saicone.mcode.module.script;

import com.saicone.mcode.module.script.condition.Compare;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Script {

    private static final Map<EvalKey, EvalBuilder<? extends Action>> ACTIONS = new HashMap<>();
    private static final Map<EvalKey, EvalBuilder<? extends Condition>> CONDITIONS = new HashMap<>();
    private static final Map<EvalKey, ScriptFunction<EvalUser, ActionResult>> COMPILED_ACTIONS = new HashMap<>();
    private static final Map<EvalKey, ScriptFunction<EvalUser, Boolean>> COMPILED_CONDITIONS = new HashMap<>();

    static {
        putCondition(EvalKey.regex("(?i)compare|eval"), object -> new Compare(String.valueOf(object)));
    }

    private Object loaded;
    private ScriptFunction<EvalUser, ?> compiled;

    public Object getLoaded() {
        return loaded;
    }

    public ScriptFunction<EvalUser, ?> getCompiled() {
        return compiled;
    }

    @Nullable
    @Contract("_, !null -> !null")
    @SuppressWarnings("unchecked")
    public <T> T apply(@NotNull EvalUser user, @Nullable T def) {
        if (compiled == null) {
            return def;
        }
        final Object result = compiled.apply(user);
        if (result == null) {
            return def;
        }
        try {
            return (T) result;
        } catch (ClassCastException e) {
            return def;
        }
    }

    @NotNull
    public ActionResult run(@NotNull EvalUser user) {
        return run(user, ActionResult.DONE);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public ActionResult run(@NotNull EvalUser user, @Nullable ActionResult def) {
        return apply(user, def);
    }

    public boolean eval(@NotNull EvalUser user) {
        return eval(user, false);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public Boolean eval(@NotNull EvalUser user, @Nullable Boolean def) {
        return apply(user, def);
    }

    public void load(@Nullable Object loaded, @Nullable ScriptFunction<EvalUser, ?> compiled) {
        this.loaded = loaded;
        this.compiled = compiled;
    }

    public void loadAction(@Nullable Object object) {
        load(object, compileAction(object));
    }

    public void loadAction(@Nullable Object action, @Nullable Object object) {
        load(action, compileAction(action, object));
    }

    public void loadCondition(@Nullable Object condition) {
        loadCondition(condition, null);
    }

    public void loadCondition(@Nullable Object condition, @Nullable Object object) {
        load(condition, compileCondition(condition, object));
    }

    @Override
    public String toString() {
        return String.valueOf(loaded);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Script script = (Script) o;

        return Objects.equals(loaded, script.loaded);
    }

    @Override
    public int hashCode() {
        return loaded != null ? loaded.hashCode() : 0;
    }

    @Nullable
    public static ScriptFunction<EvalUser, ActionResult> compileAction(@Nullable Object action) {
        return compileAction(action, null);
    }

    @Nullable
    public static ScriptFunction<EvalUser, ActionResult> compileAction(@Nullable Object action, @Nullable Object object) {
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
                function = (user) -> {
                    for (ScriptFunction<EvalUser, ActionResult> act : actions) {
                        final ActionResult result = act.apply(user);
                        if (result != ActionResult.DONE) {
                            return result;
                        }
                    }
                    return ActionResult.DONE;
                };
            }
        } else if (action instanceof Object[]) {
            function = getAction(asString((Object[]) action), object);
        } else {
            function = getAction(String.valueOf(action), object);
        }
        return function;
    }

    @Nullable
    public static ScriptFunction<EvalUser, Boolean> compileCondition(@Nullable Object condition) {
        return compileCondition(condition, null);
    }

    @Nullable
    public static ScriptFunction<EvalUser, Boolean> compileCondition(@Nullable Object condition, @Nullable Object object) {
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

    @Nullable
    public static ScriptFunction<EvalUser, ActionResult> getAction(@NotNull String id, @Nullable Object object) {
        final String[] split = id.split("[:=;*]", 2);
        if (split.length < 2) {
            return getAction(id.trim(), null, object);
        } else {
            return getAction(split[0].trim(), split[1].startsWith(" ") ? split[1].substring(1) : split[1], object);
        }
    }

    @Nullable
    public static ScriptFunction<EvalUser, ActionResult> getAction(@NotNull String id, @Nullable Object value, @Nullable Object object) {
        String finalId = id.replace("-", "").replace(" ", "").toLowerCase();
        switch (finalId) {
            case "done":
                return Action.DONE;
            case "return":
                return Action.RETURN;
            case "break":
            case "stop":
                return Action.BREAK;
            case "continue":
                return Action.CONTINUE;
            default:
                break;
        }
        final EvalBuilder<? extends Action> builder = ACTIONS.get(finalId);
        if (builder != null) {
            final Action action = builder.build(value);
            if (action != null) {
                return action.compile(object);
            }
        } else {
            return COMPILED_ACTIONS.get(finalId);
        }
        return null;
    }

    @NotNull
    public static List<ScriptFunction<EvalUser, Boolean>> getConditions(@Nullable Object condition, @Nullable Object object) {
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
    public static ScriptFunction<EvalUser, Boolean> getCondition(@NotNull String id, @Nullable Object object) {
        final String[] split = id.split("[:=;*]", 2);
        if (split.length < 2) {
            return getCondition(id.trim(), null, object);
        } else {
            return getCondition(split[0].trim(), split[1].startsWith(" ") ? split[1].substring(1) : split[1], object);
        }
    }

    @Nullable
    public static ScriptFunction<EvalUser, Boolean> getCondition(@NotNull String id, @Nullable String value, @Nullable Object object) {
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

        if (!CONDITIONS.containsKey(finalId) && !COMPILED_CONDITIONS.containsKey(finalId)) {
            if (finalId.startsWith("has") || finalId.startsWith("are")) {
                finalId = finalId.substring(3);
            } else if (finalId.startsWith("is")) {
                finalId = finalId.substring(2);
            }
        }

        final ScriptFunction<EvalUser, Boolean> function;
        final EvalBuilder<? extends Condition> builder = CONDITIONS.get(finalId);
        if (builder != null) {
            final Condition condition = builder.build(value);
            if (condition != null) {
                function = condition.compile(object);
            } else {
                function = null;
            }
        } else {
            function = COMPILED_CONDITIONS.get(finalId);
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
    public static EvalBuilder<? extends Action> putAction(@NotNull Object key, @NotNull EvalBuilder<? extends Action> action) {
        return ACTIONS.put(EvalKey.of(key), action);
    }

    @Nullable
    public static ScriptFunction<EvalUser, ActionResult> putActionFunction(@NotNull Object key, @NotNull ScriptFunction<EvalUser, ActionResult> action) {
        return COMPILED_ACTIONS.put(EvalKey.of(key), action);
    }

    @Nullable
    public static EvalBuilder<? extends Condition> putCondition(@NotNull Object key, @NotNull EvalBuilder<? extends Condition> condition) {
        return CONDITIONS.put(EvalKey.of(key), condition);
    }

    @Nullable
    public static ScriptFunction<EvalUser, Boolean> putConditionFunction(@NotNull Object key, @NotNull ScriptFunction<EvalUser, Boolean> condition) {
        return COMPILED_CONDITIONS.put(EvalKey.of(key), condition);
    }

    @Nullable
    private static ScriptFunction<EvalUser, ActionResult> concat(@Nullable ScriptFunction<EvalUser, ActionResult> base, @Nullable ScriptFunction<EvalUser, ActionResult> other) {
        if (other == null) {
            return base;
        }
        if (base == null) {
            return other;
        }
        return base.ifAnd(ActionResult.DONE, other);
    }

    @NotNull
    private static String asString(@NotNull Object[] objects) {
        final StringBuilder builder = new StringBuilder();
        for (Object o : objects) {
            builder.append(o);
        }
        return builder.toString();
    }
}
