package com.saicone.mcode.module.settings.node;

import com.saicone.mcode.util.function.ThrowableFunction;
import com.saicone.mcode.util.type.OptionalType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class NodeValue extends OptionalType {

    public static final NodeValue EMPTY = new NodeValue(null);

    private Object valueType;

    @NotNull
    public static NodeValue of(@Nullable Object object) {
        if (object instanceof NodeValue) {
            return (NodeValue) object;
        }
        return object == null ? empty() : new NodeValue(object);
    }

    @NotNull
    public static NodeValue empty() {
        return EMPTY;
    }

    public NodeValue(Object value) {
        super(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T by(@NotNull Class<T> type, @NotNull ThrowableFunction<@Nullable Object, @Nullable T> function, @Nullable T def) {
        if (type.isInstance(getValue())) {
            return value();
        } else if (type.isInstance(valueType)) {
            try {
                return (T) valueType;
            } catch (ClassCastException ignored) { }
        } else if (def == null) {
            if (valueType != null) {
                valueType = null;
            }
            return null;
        }

        Object finalValue = null;
        if (Iterable.class.isAssignableFrom(type) || type.isArray()) {
            finalValue = getValue();
        } else {
            for (Object o : this) {
                finalValue = o;
                break;
            }
        }
        if (finalValue instanceof Boolean && Number.class.isAssignableFrom(type)) {
            return (T) (valueType = by(object -> function.apply(Boolean.TRUE.equals(object) ? "1" : "0"), def));
        }
        return (T) (valueType = by(function, def));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, C extends Collection<T>> @NotNull C asCollection(@NotNull C collection, @NotNull Function<@NotNull OptionalType, @Nullable T> function) {
        if (valueType != null) {
            try {
                return (C) valueType;
            } catch (ClassCastException ignored) { }
        }
        return (C) (valueType = super.asCollection(collection, function));
    }

    @NotNull
    public List<Object> asList() {
        return asList(OptionalType::asObject);
    }

    @NotNull
    public List<String> asStringList() {
        return asList(OptionalType::asString);
    }

    @NotNull
    public List<Character> asCharList() {
        return asList(OptionalType::asChar);
    }

    @NotNull
    public List<Boolean> asBooleanList() {
        return asList(OptionalType::asBoolean);
    }

    @NotNull
    public List<Byte> asByteList() {
        return asList(OptionalType::asByte);
    }

    @NotNull
    public List<Short> asShortList() {
        return asList(OptionalType::asShort);
    }

    @NotNull
    public List<Integer> asIntList() {
        return asList(OptionalType::asInt);
    }

    @NotNull
    public List<Float> asFloatList() {
        return asList(OptionalType::asFloat);
    }

    @NotNull
    public List<Long> asLongList() {
        return asList(OptionalType::asLong);
    }

    @NotNull
    public List<Double> asDoubleList() {
        return asList(OptionalType::asDouble);
    }
}
