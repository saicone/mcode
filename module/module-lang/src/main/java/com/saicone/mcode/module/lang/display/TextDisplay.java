package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.Display;
import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.platform.MC;
import com.saicone.mcode.util.DMap;
import com.saicone.mcode.util.text.MStrings;
import com.saicone.mcode.util.text.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class TextDisplay<SenderT> implements Display<SenderT> {

    private final String text;
    private final int centerWidth;
    private final Map<String, Set<Event>> events;

    public TextDisplay(@NotNull String text, int centerWidth, @NotNull Map<String, Set<Event>> events) {
        this.text = text;
        this.centerWidth = centerWidth;
        this.events = events;
    }

    @Override
    public @Nullable Object get(@NotNull String field) {
        switch (field.toLowerCase()) {
            case "text":
            case "string":
            case "value":
                return text;
            case "centerwidth":
                return centerWidth;
            default:
                return null;
        }
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
    public Map<String, Set<Event>> getEvents() {
        return events;
    }

    @NotNull
    public Set<Event> getEvents(@NotNull String id) {
        return events.getOrDefault(id, Set.of());
    }

    @NotNull
    public Map<String, Set<Event>> getParsedEvents(@NotNull Function<String, String> parser) {
        final Map<String, Set<Event>> map = new HashMap<>();
        for (var entry : events.entrySet()) {
            map.put(entry.getKey(), entry.getValue().stream().map(event -> event.parse(parser)).collect(Collectors.toSet()));
        }
        return map;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
        if (events.isEmpty()) {
            sendParsed(type, parser.apply(text));
        } else {
            sendParsed(type, parser.apply(text), getParsedEvents(parser));
        }
    }

    @Override
    public void sendTo(@NotNull Collection<? extends SenderT> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
        final String text = parser.apply(this.text);
        if (events.isEmpty()) {
            for (SenderT player : senders) {
                sendParsed(player, playerParser.apply(player, text));
            }
        } else {
            final Map<String, Set<Event>> events = getParsedEvents(parser);
            for (SenderT player : senders) {
                sendParsed(player, playerParser.apply(player, text), events);
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

    private void sendParsed(@NotNull SenderT type, @NotNull String text, @NotNull Map<String, Set<Event>> events) {
        final Builder<SenderT> builder = newBuilder();
        Strings.findInside(text, "<event.", "</event>", (s, found) -> {
            if (found) {
                final int index = s.indexOf('>');
                if (index > 0 && index + 1 < s.length()) {
                    final String str = s.substring(index + 1);
                    builder.sum(MStrings.getFontLength(str));
                    builder.append(type, str, events.getOrDefault(s.substring(0, index), Set.of()));
                } else {
                    builder.append("<event." + s + "</event>");
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

        public abstract void append(@NotNull SenderT type, @NotNull String s, @NotNull Set<Event> events);

        public abstract void sendTo(@NotNull SenderT type);

        protected int protocol(@NotNull SenderT type) {
            return MC.version().protocol();
        }

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
        public @Nullable TextDisplay<SenderT> load(@Nullable Object object) {
            return (TextDisplay<SenderT>) super.load(object);
        }

        @Override
        public @Nullable TextDisplay<SenderT> load(@NotNull String text) {
            return newTextDisplay(text, -1, Map.of());
        }

        @Override
        public @Nullable TextDisplay<SenderT> load(@NotNull List<Object> list) {
            return newTextDisplay(joinIterable(list), -1, Map.of());
        }

        @Override
        public @Nullable TextDisplay<SenderT> load(@NotNull DMap map) {
            final Object obj = map.getRegex("(?i)value|text");
            if (obj == null) {
                return null;
            }
            final String text;
            if (obj instanceof Iterable) {
                text = joinIterable((Iterable<?>) obj);
            } else {
                text = String.valueOf(obj);
            }

            final Object centered = map.getRegex("(?i)(chat-?)?width|centered");
            final int centerWidth;
            if (centered instanceof Boolean) {
                if ((Boolean) centered) {
                    centerWidth = MStrings.CHAT_WIDTH;
                } else {
                    centerWidth = -1;
                }
            } else {
                final String s = String.valueOf(centered);
                if (Strings.isNumber(s, Integer.class)) {
                    centerWidth = Integer.parseInt(s);
                } else if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes")) {
                    centerWidth = MStrings.CHAT_WIDTH;
                } else {
                    centerWidth = -1;
                }
            }

            final Map<String, Set<Event>> events = new HashMap<>();
            map.forEach(key -> key.matches("(?i)((chat|text)-?)?events?(\\..*)?"), (key, value) -> {
                final int index = key.indexOf('.');
                if (index > 0) {
                    events.put(key.substring(index + 1), loadEvents(value));
                } else if (value instanceof Map) {
                    for (var entry : ((Map<?, ?>) value).entrySet()) {
                        events.put(String.valueOf(entry.getKey()), loadEvents(entry.getValue()));
                    }
                }
            });
            return newTextDisplay(text, centerWidth, events);
        }

        @NotNull
        protected Set<Event> loadEvents(@Nullable Object value) {
            if (!(value instanceof Map)) {
                return Set.of();
            }
            final Set<Event> types = new HashSet<>();
            for (var e : ((Map<?, ?>) value).entrySet()) {
                final String key = String.valueOf(e.getKey()).trim().toUpperCase().replace('-', '_');
                switch (key) {
                    case "OPEN_URL":
                    case "OPEN_FILE":
                    case "RUN_COMMAND":
                    case "SUGGEST_COMMAND":
                    case "CHANGE_PAGE":
                    case "COPY_TO_CLIPBOARD":
                        types.add(newEvent(ClickAction.of(key), String.valueOf(e.getValue())));
                        break;
                    case "SHOW_TEXT":
                        types.add(newEvent(HoverAction.SHOW_TEXT, Strings.join("\n", e.getValue())));
                        break;
                    case "SHOW_ITEM":
                    case "SHOW_ENTITY":
                        types.add(newEvent(HoverAction.of(key), e.getValue()));
                        break;
                    default:
                        break;
                }
            }
            return types;
        }

        protected abstract void sendText(@NotNull SenderT type, @NotNull String text);

        @NotNull
        protected abstract Builder<SenderT> newBuilder();

        @NotNull
        @SuppressWarnings("unchecked")
        protected Event newEvent(@NotNull Action action, @NotNull Object value) {
            if (action.isHover() && action != HoverAction.SHOW_TEXT) {
                final DMap data;
                if (value instanceof Map) {
                    data = new DMap((Map<String, Object>) value);
                } else {
                    data = new DMap(Map.of("value", String.valueOf(value)));
                }
                final Map<String, Object> map = new HashMap<>();
                if (action == HoverAction.SHOW_ITEM) {
                    map.put("id", data.getBy(String::valueOf, m -> m.getRegex("(?i)value|id|material")));
                    map.put("count", data.getBy(o -> Integer.parseInt(String.valueOf(o)), m -> m.getRegex("(?i)count|amount")));
                    map.put("tag", data.getBy(String::valueOf, m -> m.getRegex("(?i)tag|nbt")));
                } else if (action == HoverAction.SHOW_ENTITY) {
                    map.put("name", data.getBy(String::valueOf, m -> m.getRegex("(?i)(display-?)?name")));
                    map.put("type", data.getBy(String::valueOf, m -> m.getRegex("(?i)value|type")));
                    map.put("id", data.getBy(o -> UUID.fromString(String.valueOf(o)), m -> m.getRegex("(?i)id|unique-?id")));
                }
                return new Event(action, map);
            }
            return new Event(action, value);
        }

        @NotNull
        private String joinIterable(@NotNull Iterable<?> list) {
            final List<String> text = new ArrayList<>();
            for (Object o : list) {
                text.add(String.valueOf(o));
            }
            return String.join("\n", text);
        }

        @NotNull
        private TextDisplay<SenderT> newTextDisplay(@NotNull String text, int centerWidth, @NotNull Map<String, Set<Event>> actions) {
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

    @SuppressWarnings("unchecked")
    public static class Event {
        private final Action action;
        private final Object value;

        public Event(@NotNull Action action, @NotNull Object value) {
            this.action = action;
            this.value = value;
        }

        @NotNull
        public Event parse(@NotNull Function<String, String> parser) {
            if (value instanceof String) {
                return new Event(action, parser.apply((String) value));
            } else if (value instanceof Map) {
                final Map<String, Object> map = new HashMap<>();
                for (var entry : ((Map<String, Object>) value).entrySet()) {
                    if (entry.getValue() instanceof String) {
                        map.put(entry.getKey(), parser.apply((String) entry.getValue()));
                    } else {
                        map.put(entry.getKey(), entry.getValue());
                    }
                }
                return new Event(action, map);
            } else {
                return this;
            }
        }

        @NotNull
        public Action getAction() {
            return action;
        }

        @NotNull
        public Object getValue() {
            return value;
        }

        @NotNull
        public String getString() {
            return (String) value;
        }

        @NotNull
        public String getItemId() {
            return (String) ((Map<String, Object>) value).get("id");
        }

        public int getItemCount() {
            return (int) ((Map<String, Object>) value).getOrDefault("count", 1);
        }

        @Nullable
        @Deprecated
        public String getItemTag() {
            return (String) ((Map<String, Object>) value).get("tag");
        }

        @Nullable
        public String getItemComponents() {
            return (String) ((Map<String, Object>) value).getOrDefault("components", "{}");
        }

        @Nullable
        public String getEntityName() {
            return (String) ((Map<String, Object>) value).get("name");
        }

        @NotNull
        public String getEntityType() {
            return (String) ((Map<String, Object>) value).get("type");
        }

        @NotNull
        public UUID getEntityUniqueId() {
            return (UUID) ((Map<String, Object>) value).get("id");
        }
    }

    public interface Action {

        default boolean isClick() {
            return false;
        }

        default boolean isHover() {
            return false;
        }

        int ordinal();

        @NotNull
        default ClickAction click() {
            return (ClickAction) this;
        }

        @NotNull
        default HoverAction hover() {
            return (HoverAction) this;
        }
    }

    public enum ClickAction implements Action {
        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE,
        COPY_TO_CLIPBOARD;

        @Override
        public boolean isClick() {
            return true;
        }

        @Nullable
        public static ClickAction of(@NotNull String name) {
            for (ClickAction value : values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            return null;
        }
    }

    public enum HoverAction implements Action {
        SHOW_TEXT,
        SHOW_ITEM,
        SHOW_ENTITY;

        @Override
        public boolean isHover() {
            return true;
        }

        @Nullable
        public static HoverAction of(@NotNull String name) {
            for (HoverAction value : values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            return null;
        }
    }
}
