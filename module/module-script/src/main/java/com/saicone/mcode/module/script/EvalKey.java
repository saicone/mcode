package com.saicone.mcode.module.script;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class EvalKey {

    private final String id;

    @NotNull
    public static EvalKey of(@NotNull Object id) {
        if (id instanceof EvalKey) {
            return (EvalKey) id;
        }
        return new EvalKey(String.valueOf(id));
    }

    @NotNull
    public static EvalKey regex(@NotNull @Language("RegExp") String regex) {
        return new Regex(regex);
    }

    public EvalKey(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public boolean compare(@NotNull String s) {
        return this.id.equalsIgnoreCase(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof String) {
            return compare((String) o);
        }
        if (getClass() != o.getClass()) return false;

        EvalKey evalKey = (EvalKey) o;

        return id.equals(evalKey.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static class Regex extends EvalKey {

        private final Pattern pattern;

        public Regex(@NotNull @Language("RegExp") String regex) {
            super(regex);
            this.pattern = Pattern.compile(regex);
        }

        @NotNull
        public Pattern getPattern() {
            return pattern;
        }

        @Override
        public boolean compare(@NotNull String s) {
            return pattern.matcher(s).matches();
        }
    }
}
