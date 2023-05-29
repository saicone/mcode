package com.saicone.mcode.module.script;

import com.saicone.mcode.util.DMap;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Action implements Eval<ActionResult> {

    public static final ScriptFunction<EvalUser, ActionResult> DONE = (user) -> ActionResult.DONE;
    public static final ScriptFunction<EvalUser, ActionResult> RETURN = (user) -> ActionResult.RETURN;
    public static final ScriptFunction<EvalUser, ActionResult> BREAK = (user) -> ActionResult.BREAK;
    public static final ScriptFunction<EvalUser, ActionResult> CONTINUE = (user) -> ActionResult.CONTINUE;

    @Override
    public @Nullable ScriptFunction<EvalUser, ActionResult> compile() {
        return this::run;
    }

    @NotNull
    public <T extends EvalUser> ActionResult run(@NotNull T user) {
        return ActionResult.DONE;
    }

    public static class Builder<A extends Action> implements EvalBuilder<A> {

        private final @Language("RegExp") String regex;
        private final Pattern pattern;

        private Function<DMap, A> mapFunction;
        private Function<List<Object>, A> listFunction;
        private Function<String, A> textFunction;

        public Builder(@NotNull @Language("RegExp") String regex) {
            this.regex = regex;
            this.pattern = Pattern.compile(regex);
        }

        @NotNull
        public @Language("RegExp") String getRegex() {
            return regex;
        }

        @NotNull
        public Pattern getPattern() {
            return pattern;
        }

        public void register() {
            Script.putAction(EvalKey.regex(regex), this);
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<A> map(@NotNull Function<DMap, A> mapFunction) {
            this.mapFunction = mapFunction;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<A> list(@NotNull Function<List<Object>, A> listFunction) {
            this.listFunction = listFunction;
            return this;
        }

        @NotNull
        @Contract("_, _ -> this")
        public <T> Builder<A> list(@NotNull Function<Object, T> mapper, @NotNull Function<List<T>, A> listFunction) {
            this.listFunction = list -> listFunction.apply(list.stream().map(mapper).collect(Collectors.toList()));
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<A> text(@NotNull Function<String, A> textFunction) {
            this.textFunction = textFunction;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nullable A build(@Nullable Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Map) {
                Map<String, Object> map;
                try {
                    map = (Map<String, Object>) object;
                } catch (ClassCastException e) {
                    map = new HashMap<>();
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                        map.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                }
                return build(map);
            } else if (object instanceof List) {
                return build((List<Object>) object);
            } else if (object instanceof Iterable) {
                final List<Object> list = new ArrayList<>();
                for (Object o : (Iterable<?>) object) {
                    list.add(o);
                }
                return build(list);
            } else {
                return build(String.valueOf(object));
            }
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public A build(@NotNull Map<String, Object> map) {
            final DMap dmap = new DMap(map);
            if (mapFunction != null) {
                return mapFunction.apply(dmap);
            }
            final Object value = dmap.getRegex("(?i)value|text|list");
            if (value == null) {
                return null;
            }
            if (value instanceof List) {
                if (listFunction != null) {
                    return listFunction.apply((List<Object>) value);
                } else if (textFunction != null) {
                    return textFunction.apply(((List<Object>) value).stream().map(String::valueOf).collect(Collectors.joining("\n")));
                }
            } else if (textFunction != null) {
                return textFunction.apply(String.valueOf(value));
            }
            return null;
        }

        @Nullable
        public A build(@NotNull List<Object> list) {
            if (listFunction != null) {
                return listFunction.apply(list);
            }
            final Map<String, Object> map = new HashMap<>();
            for (Object o : list) {
                final String[] split = String.valueOf(o).split("=", 2);
                if (split.length == 2) {
                    map.put(split[0], split[1]);
                } else {
                    map.put(String.valueOf(o), "");
                }
            }
            return build(map);
        }

        @Nullable
        public A build(@NotNull String text) {
            if (textFunction != null) {
                return textFunction.apply(text);
            }
            final Map<String, Object> map = new HashMap<>();
            map.put("value", text);
            return build(map);
        }
    }
}
