package com.saicone.mcode.module.script;

import com.saicone.mcode.util.DMap;
import com.saicone.mcode.util.function.ThrowableFunction;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Action implements Eval<ActionResult> {

    public static final ScriptFunction<EvalUser, ActionResult> DONE = (user) -> ActionResult.DONE;
    public static final ScriptFunction<EvalUser, ActionResult> RETURN = (user) -> ActionResult.RETURN;
    public static final ScriptFunction<EvalUser, ActionResult> BREAK = (user) -> ActionResult.BREAK;
    public static final ScriptFunction<EvalUser, ActionResult> CONTINUE = (user) -> ActionResult.CONTINUE;

    private BiConsumer<EvalUser, Action> consumer;

    public void setConsumer(@Nullable BiConsumer<EvalUser, Action> consumer) {
        this.consumer = consumer;
    }

    @Nullable
    public BiConsumer<EvalUser, Action> getConsumer() {
        return consumer;
    }

    @Override
    public @Nullable ScriptFunction<EvalUser, ActionResult> compile() {
        return (user) -> {
            if (consumer != null) {
                consumer.accept(user, this);
            }
            return run(user);
        };
    }

    @NotNull
    public ActionResult run(@NotNull EvalUser user) {
        return ActionResult.DONE;
    }

    public static class Builder<ActionT extends Action> implements EvalBuilder<ActionT> {

        private final @Language("RegExp") String regex;
        private final Pattern pattern;

        protected ThrowableFunction<DMap, ActionT> mapFunction;
        protected ThrowableFunction<List<Object>, ActionT> listFunction;
        protected ThrowableFunction<String, ActionT> textFunction;

        protected BiConsumer<EvalUser, ActionT> consumer;

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
            register(Script.REGISTRY);
        }

        public void register(@NotNull ScriptRegistry registry) {
            registry.putAction(EvalKey.regex(regex), this);
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<ActionT> map(@NotNull ThrowableFunction<DMap, ActionT> mapFunction) {
            this.mapFunction = mapFunction;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<ActionT> list(@NotNull ThrowableFunction<List<Object>, ActionT> listFunction) {
            this.listFunction = listFunction;
            return this;
        }

        @NotNull
        @Contract("_, _ -> this")
        public <T> Builder<ActionT> list(@NotNull Function<Object, T> mapper, @NotNull ThrowableFunction<List<T>, ActionT> listFunction) {
            this.listFunction = list -> listFunction.apply(list.stream().map(mapper).collect(Collectors.toList()));
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<ActionT> text(@NotNull ThrowableFunction<String, ActionT> textFunction) {
            this.textFunction = textFunction;
            return this;
        }

        @NotNull
        @Contract("_, _ -> this")
        public Builder<ActionT> textList(@NotNull ThrowableFunction<String, String[]> mapper, @NotNull ThrowableFunction<List<String>, ActionT> listFunction) {
            list(String::valueOf, listFunction);
            this.textFunction = s -> listFunction.apply(List.of(mapper.apply(s)));
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<ActionT> consumer(@NotNull BiConsumer<EvalUser, ActionT> consumer) {
            this.consumer = consumer;
            return this;
        }

        @NotNull
        @Contract("_, _ -> this")
        public <A> Builder<ActionT> consumer(@NotNull ThrowableFunction<EvalUser, A> userMapper, @NotNull BiConsumer<A, ActionT> consumer) {
            this.consumer = (user, action) -> {
                final A a;
                try {
                    a = userMapper.apply(user);
                } catch (Throwable t) {
                    return;
                }
                consumer.accept(a, action);
            };
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nullable ActionT build(@Nullable Object object) {
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
        public ActionT build(@NotNull Map<String, Object> map) {
            final DMap dmap = new DMap(map);
            try {
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
            } catch (Throwable ignored) { }
            return null;
        }

        @Nullable
        public ActionT build(@NotNull List<Object> list) {
            if (listFunction != null) {
                try {
                    return listFunction.apply(list);
                } catch (Throwable t) {
                    return null;
                }
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
        public ActionT build(@NotNull String text) {
            if (textFunction != null) {
                try {
                    return textFunction.apply(text);
                } catch (Throwable t) {
                    return null;
                }
            }
            final Map<String, Object> map = new HashMap<>();
            map.put("value", text);
            return build(map);
        }
    }
}
