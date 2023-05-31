package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.MStrings;
import com.saicone.mcode.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public abstract class TextDisplay<SenderT> extends Display<SenderT> {

    private final String text;
    private final int centerWidth;
    private final Map<String, Map<Object, String>> actions;

    public TextDisplay(@NotNull String text, int centerWidth, @NotNull Map<String, Map<Object, String>> actions) {
        this.text = text;
        this.centerWidth = centerWidth;
        this.actions = actions;
    }

    @NotNull
    @Override
    public String getText() {
        return text;
    }

    public int getCenterWidth() {
        return centerWidth;
    }

    @NotNull
    public Map<String, Map<Object, String>> getActions() {
        return actions;
    }

    @NotNull
    public Map<Object, String> getActions(@NotNull String id) {
        return actions.getOrDefault(id, Map.of());
    }

    @NotNull
    public Map<String, Map<Object, String>> getParsedActions(@NotNull Function<String, String> parse) {
        final Map<String, Map<Object, String>> map = new HashMap<>();
        for (var entry : actions.entrySet()) {
            final Map<Object, String> value = new HashMap<>();
            for (var valueEntry : entry.getValue().entrySet()) {
                value.put(valueEntry.getKey(), parse.apply(valueEntry.getValue()));
            }
            map.put(entry.getKey(), value);
        }
        return map;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
        if (actions.isEmpty()) {
            sendParsed(type, parser.apply(text));
        } else {
            sendParsed(type, parser.apply(text), getParsedActions(parser));
        }
    }

    @Override
    public void sendToAll(@NotNull Function<String, String> parser) {
        final String text = parser.apply(this.text);
        if (actions.isEmpty()) {
            for (SenderT player : players()) {
                sendParsed(player, Text.of(text).parse(player).toString());
            }
        } else {
            final Map<String, Map<Object, String>> actions = getParsedActions(parser);
            for (SenderT player : players()) {
                sendParsed(player, Text.of(text).parse(player).toString(), actions);
            }
        }
    }

    private void sendParsed(@NotNull SenderT type, @NotNull String text) {
        if (centerWidth > 0) {
            sendText(type, MStrings.centerText(text, centerWidth));
        } else {
            sendText(type, text);
        }
    }

    private void sendParsed(@NotNull SenderT type, @NotNull String text, @NotNull Map<String, Map<Object, String>> actions) {
        final Builder<SenderT> builder = newBuilder();
        Strings.findInside(text, "[action=", "[/action]", (s, found) -> {
            if (found) {
                final int index = s.indexOf(']');
                if (index > 0 && index + 1 < s.length()) {
                    final String str = s.substring(index + 1);
                    builder.sum(MStrings.getFontLength(str));
                    builder.append(str, actions.getOrDefault(s.substring(0, index), Map.of()));
                } else {
                    builder.append("[action=" + s + "[/action]");
                }
            } else {
                builder.append(s);
            }
        });
        builder.sendTo(type, centerWidth);
    }

    protected abstract void sendText(@NotNull SenderT type, @NotNull String text);

    protected abstract Builder<SenderT> newBuilder();

    public static abstract class Builder<SenderT> {

        private int widthCount = 0;

        public void sum(int px) {
            widthCount = widthCount + px;
        }

        public void append(@NotNull String s) {
            append(s, false);
        }

        public abstract void append(@NotNull String s, boolean before);

        public abstract void append(@NotNull String s, @NotNull Map<Object, String> actions);

        public abstract void sendTo(@NotNull SenderT type);

        public void sendTo(@NotNull SenderT type, int width) {
            if (width > 0) {
                final String s = MStrings.spacesToCenter(widthCount, width);
                if (!s.isEmpty()) {
                    append(s, true);
                }
            }
            sendTo(type);
        }
    }

    public static abstract class Loader<SenderT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)text|messages?|msg", Map.of("text", ""));
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull String text) {
            return newTextDisplay(text, -1, Map.of());
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull List<Object> list) {
            return newTextDisplay(joinList(list), -1, Map.of());
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull Map<String, Object> map) {
            final Object obj = get(map, "text");
            if (obj == null) {
                return null;
            }
            final String text;
            if (obj instanceof List) {
                text = joinList((List<?>) obj);
            } else {
                text = String.valueOf(obj);
            }

            final Object centered = get(map, "centered");
            final int centerWidth;
            if (centered instanceof Boolean) {
                if ((Boolean) centered) {
                    centerWidth = MStrings.getChatWidth();
                } else {
                    centerWidth = -1;
                }
            } else if (Boolean.TRUE.equals(asBoolean(centered, null))) {
                centerWidth = MStrings.getChatWidth();
            } else {
                centerWidth = asInt(centered, -1);
            }

            final Object actionsObject = get(map, "actions");
            if (actionsObject instanceof Map) {
                final Map<String, Map<Object, String>> actions = new HashMap<>();
                ((Map<?, ?>) actionsObject).forEach((id, list) -> {
                    if (list instanceof List) {
                        final Map<Object, String> types = new HashMap<>();
                        for (Object o : (List<?>) list) {
                            if (o instanceof Map) {
                                Object type = get((Map<?, ?>) o, "type");
                                if (type == null) continue;

                                type = parseAction(String.valueOf(type));
                                if (type == null) continue;

                                Object value = get((Map<?, ?>) o, "value");
                                if (value == null) continue;

                                if (value instanceof List) {
                                    List<String> strings = new ArrayList<>();
                                    for (Object s : (List<?>) value) {
                                        strings.add(String.valueOf(s).replace("\\n", "\n"));
                                    }
                                    value = String.join("\n", strings);
                                } else {
                                    value = String.valueOf(value).replace("\\n", "\n");
                                }

                                types.put(type, (String) value);
                            }
                        }
                        actions.put(String.valueOf(id), types);
                    } else {
                        actions.put(String.valueOf(id), Map.of());
                    }
                });
                return newTextDisplay(text, centerWidth, actions);
            } else {
                return newTextDisplay(text, centerWidth, Map.of());
            }
        }

        @Nullable
        protected abstract Object parseAction(@NotNull String s);

        protected abstract void sendText(@NotNull SenderT type, @NotNull String text);

        protected abstract Builder<SenderT> newBuilder();

        @NotNull
        private String joinList(@NotNull List<?> list) {
            final List<String> text = new ArrayList<>();
            for (Object o : list) {
                text.add(String.valueOf(o));
            }
            return String.join("\n", text);
        }

        @NotNull
        private TextDisplay<SenderT> newTextDisplay(@NotNull String text, int centerWidth, @NotNull Map<String, Map<Object, String>> actions) {
            return new TextDisplay<>(text, centerWidth, actions) {
                @Override
                protected void sendText(@NotNull SenderT type, @NotNull String text) {
                    Loader.this.sendText(type, text);
                }

                @Override
                protected Builder<SenderT> newBuilder() {
                    return Loader.this.newBuilder();
                }
            };
        }
    }
}
