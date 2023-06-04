package com.saicone.mcode.module.script;

import com.saicone.mcode.util.function.ThrowableFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ScriptRegistry {

    private final Map<EvalKey, EvalBuilder<? extends Action>> actions = new HashMap<>();
    private final Map<EvalKey, EvalBuilder<? extends Condition>> conditions = new HashMap<>();
    private final Map<EvalKey, ScriptFunction<EvalUser, ActionResult>> compiledActions = new HashMap<>();
    private final Map<EvalKey, ScriptFunction<EvalUser, Boolean>> compiledConditions = new HashMap<>();

    @SuppressWarnings("all")
    public boolean containsAnyAction(@NotNull Object key) {
        return actions.containsKey(key) || compiledActions.containsKey(key);
    }

    @SuppressWarnings("all")
    public boolean containsAnyCondition(@NotNull Object key) {
        return conditions.containsKey(key) || compiledConditions.containsKey(key);
    }

    @NotNull
    public Map<EvalKey, EvalBuilder<? extends Action>> getActions() {
        return actions;
    }

    @Nullable
    @SuppressWarnings("all")
    public EvalBuilder<? extends Action> getAction(@NotNull Object key) {
        return actions.get(key);
    }

    @NotNull
    public Map<EvalKey, EvalBuilder<? extends Condition>> getConditions() {
        return conditions;
    }

    @Nullable
    @SuppressWarnings("all")
    public EvalBuilder<? extends Condition> getCondition(@NotNull Object key) {
        return conditions.get(key);
    }

    @NotNull
    public Map<EvalKey, ScriptFunction<EvalUser, ActionResult>> getCompiledActions() {
        return compiledActions;
    }

    @Nullable
    @SuppressWarnings("all")
    public ScriptFunction<EvalUser, ActionResult> getCompiledAction(@NotNull Object key) {
        return compiledActions.get(key);
    }

    @NotNull
    public Map<EvalKey, ScriptFunction<EvalUser, Boolean>> getCompiledConditions() {
        return compiledConditions;
    }

    @Nullable
    @SuppressWarnings("all")
    public ScriptFunction<EvalUser, Boolean> getCompiledCondition(@NotNull Object key) {
        return compiledConditions.get(key);
    }

    @Nullable
    public EvalBuilder<? extends Action> putAction(@NotNull Object key, @NotNull EvalBuilder<? extends Action> action) {
        return actions.put(EvalKey.of(key), action);
    }

    @Nullable
    public ScriptFunction<EvalUser, ActionResult> putActionFunction(@NotNull Object key, @NotNull ScriptFunction<EvalUser, ActionResult> action) {
        return compiledActions.put(EvalKey.of(key), action);
    }

    @Nullable
    public EvalBuilder<? extends Condition> putCondition(@NotNull Object key, @NotNull EvalBuilder<? extends Condition> condition) {
        return conditions.put(EvalKey.of(key), condition);
    }

    @Nullable
    public <A, B> EvalBuilder<? extends Condition> putConditionPredicate(@NotNull Object key, @NotNull Predicate<String> predicate) {
        return putCondition(key, object -> Condition.of(object, predicate));
    }

    @Nullable
    public <A, B> EvalBuilder<? extends Condition> putConditionPredicate(@NotNull Object key, @NotNull BiPredicate<EvalUser, String> predicate) {
        return putCondition(key, object -> Condition.of(object, predicate));
    }

    @Nullable
    public <A, B> EvalBuilder<? extends Condition> putConditionPredicate(@NotNull Object key, @NotNull ThrowableFunction<EvalUser, A> userMapper, @NotNull ThrowableFunction<String, B> valueMapper, @NotNull BiPredicate<A, B> predicate) {
        return putCondition(key, object -> Condition.of(object, userMapper, valueMapper, predicate));
    }

    @Nullable
    public ScriptFunction<EvalUser, Boolean> putConditionFunction(@NotNull Object key, @NotNull ScriptFunction<EvalUser, Boolean> condition) {
        return compiledConditions.put(EvalKey.of(key), condition);
    }

    @Nullable
    public <A> EvalBuilder<? extends Condition> putUserCondition(@NotNull Object key, @NotNull ThrowableFunction<EvalUser, A> userMapper, @NotNull Predicate<A> predicate) {
        return putCondition(key, object -> Condition.ofUser(object, userMapper, predicate));
    }

    @Nullable
    public <A> EvalBuilder<? extends Condition> putUserCondition(@NotNull Object key, @NotNull ThrowableFunction<EvalUser, A> userMapper, @NotNull BiPredicate<A, String> predicate) {
        return putCondition(key, object -> Condition.ofUser(object, userMapper, predicate));
    }

    @Nullable
    public <B> EvalBuilder<? extends Condition> putValueCondition(@NotNull Object key, @NotNull ThrowableFunction<String, B> valueMapper, @NotNull Predicate<B> predicate) {
        return putCondition(key, object -> Condition.ofValue(object, valueMapper, predicate));
    }

    @Nullable
    public <B> EvalBuilder<? extends Condition> putValueCondition(@NotNull Object key, @NotNull ThrowableFunction<String, B> valueMapper, @NotNull BiPredicate<EvalUser, B> predicate) {
        return putCondition(key, object -> Condition.ofValue(object, valueMapper, predicate));
    }

    @SuppressWarnings("all")
    public boolean removeAction(@NotNull Object key) {
        Object result = actions.remove(key);
        if (result != null) {
            compiledActions.remove(key);
        } else {
            result = compiledActions.remove(key);
        }
        return result != null;
    }

    @SuppressWarnings("all")
    public boolean removeCondition(@NotNull Object key) {
        Object result = conditions.remove(key);
        if (result != null) {
            compiledConditions.remove(key);
        } else {
            result = compiledConditions.remove(key);
        }
        return result != null;
    }
}
