package com.saicone.mcode.module.settings.update;

import com.saicone.mcode.module.settings.Settings;
import com.saicone.mcode.module.settings.SettingsFile;
import com.saicone.mcode.module.settings.node.SettingsNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class SettingsUpdater {

    private List<Rule> rules;

    public void onOptional(@NotNull Settings base, @NotNull SettingsFile optional) {
        // empty by default
    }

    public void onUpdate(@NotNull Settings base) {
        for (Rule rule : getRules()) {
            switch (rule.action) {
                case ADD:
                    if (rule.path == null) {
                        throw new IllegalArgumentException("Cannot apply RuleAction.ADD without array path");
                    }
                    base.set(rule.path, rule.value, false);
                    break;
                case DELETE:
                    rule.getNode(base).delete(Boolean.TRUE.equals(rule.value));
                    break;
                case REPLACE:
                    rule.getNode(base).replace(rule.value);
                    break;
                case MOVE:
                    if (rule.value instanceof String[]) {
                        rule.getNode(base).move((String[]) rule.value);
                    } else {
                        throw new IllegalArgumentException("Cannot apply RuleAction.MOVE without array value");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public List<Rule> getRules() {
        if (this.rules == null) {
            final List<Rule> list = new ArrayList<>();
            for (Class<?> clazz = getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    final Object object;
                    try {
                        field.setAccessible(true);
                        object = field.get(null);
                    } catch (IllegalAccessException e) {
                        continue;
                    }

                    if (object instanceof Rule) {
                        list.add((Rule) object);
                    }
                }
            }
            this.rules = list;
        }
        return rules;
    }

    public static class Rule {

        private final String[] path;
        private final Function<Settings, SettingsNode> function;

        private RuleAction action = RuleAction.UNKNOWN;
        private Object value;

        @NotNull
        @Contract("_ -> new")
        public static Rule at(@NotNull String... path) {
            return new Rule(path);
        }

        @NotNull
        @Contract("_ -> new")
        public static Rule at(@NotNull Function<Settings, SettingsNode> function) {
            return new Rule(function);
        }

        Rule(@NotNull String... path) {
            this.path = path;
            this.function = null;
        }

        Rule(@NotNull Function<Settings, SettingsNode> function) {
            this.path = null;
            this.function = function;
        }

        @NotNull
        public SettingsNode getNode(@NotNull Settings settings) {
            if (path != null) {
                return settings.get(path);
            }
            if (function != null) {
                return function.apply(settings);
            }
            throw new NullPointerException("Cannot get SettingsNode using null path or function");
        }

        @NotNull
        @Contract("_ -> this")
        public Rule add(@Nullable Object value) {
            this.action = RuleAction.ADD;
            this.value = value;
            return this;
        }

        @NotNull
        @Contract("-> this")
        public Rule delete() {
            return delete(false);
        }

        @NotNull
        @Contract("_ -> this")
        public Rule delete(boolean deep) {
            this.action = RuleAction.DELETE;
            this.value = deep;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Rule replace(@Nullable Object value) {
            this.action = RuleAction.REPLACE;
            this.value = value;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Rule moveTo(@Nullable String... path) {
            this.action = RuleAction.MOVE;
            this.value = path;
            return this;
        }
    }

    public enum RuleAction {
        ADD, DELETE, REPLACE, MOVE, UNKNOWN;
    }

    public static class Simple extends SettingsUpdater {

        @Override
        public void onOptional(@NotNull Settings base, @NotNull SettingsFile optional) {
            optional.load();
            base.set(optional.getValue(), false);
        }
    }
}
