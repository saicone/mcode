package com.saicone.mcode.module.command;

import com.saicone.mcode.util.Dual;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class CommandKey extends Dual<@NotNull String, Pattern> {

    private final transient CommandKey parent;
    private final Map<String, Pattern> matcher = new HashMap<>();

    public CommandKey(@NotNull String name) {
        this(null, name, null);
    }

    public CommandKey(@Nullable CommandKey parent, @NotNull String name) {
        this(parent, name, null);
    }

    public CommandKey(@NotNull String name, @Nullable Pattern pattern) {
        this(null, name, pattern);
    }

    public CommandKey(@Nullable CommandKey parent, @NotNull String name, @Nullable Pattern pattern) {
        super(name, pattern);
        this.parent = parent;
    }

    public boolean matches(@NotNull String s) {
        if (s.equalsIgnoreCase(this.getLeft()) || (this.getRight() != null && this.getRight().matcher(s).matches())) {
            return true;
        }
        for (Map.Entry<String, Pattern> entry : matcher.entrySet()) {
            if (s.equalsIgnoreCase(entry.getKey()) || (entry.getValue() != null && entry.getValue().matcher(s).matches())) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(@NotNull String s) {
        return this.getLeft().equalsIgnoreCase(s) || this.matcher.containsKey(s.toLowerCase());
    }

    @Nullable
    public CommandKey getParent() {
        return parent;
    }

    @Nullable
    public Dual<String, Pattern> getMatch(@NotNull String s) {
        if (s.equalsIgnoreCase(this.getLeft()) || (this.getRight() != null && this.getRight().matcher(s).matches())) {
            return this;
        }
        for (Map.Entry<String, Pattern> entry : matcher.entrySet()) {
            if (s.equalsIgnoreCase(entry.getKey()) || (entry.getValue() != null && entry.getValue().matcher(s).matches())) {
                return Dual.of(entry);
            }
        }
        return null;
    }

    @NotNull
    public Map<String, Pattern> getMatcher() {
        return matcher;
    }

    @NotNull
    public String getName() {
        return this.getLeft();
    }

    @NotNull
    public Set<String> getAliases() {
        return matcher.keySet();
    }

    @NotNull
    @Contract("_ -> this")
    public CommandKey alias(@NotNull String... aliases) {
        for (String alias : aliases) {
            if (!contains(alias)) {
                this.matcher.put(alias.toLowerCase(), null);
            }
        }
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public CommandKey alias(@NotNull String alias, @NotNull Pattern pattern) {
        if (!contains(alias)) {
            this.matcher.put(alias.toLowerCase(), pattern);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandKey alias(@NotNull Dual<@NotNull String, Pattern>... aliases) {
        for (Dual<String, Pattern> alias : aliases) {
            if (!contains(alias.getLeft())) {
                this.matcher.put(alias.getLeft().toLowerCase(), alias.getRight());
            }
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof String) return parent == null && matches((String) o);
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CommandKey that = (CommandKey) o;

        return matcher.equals(that.matcher) && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + matcher.hashCode();
        return result;
    }
}
