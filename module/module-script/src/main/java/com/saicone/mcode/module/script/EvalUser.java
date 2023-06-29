package com.saicone.mcode.module.script;

import com.saicone.mcode.Platform;
import com.saicone.mcode.platform.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class EvalUser {

    private String id;
    private Object subject;
    private Object agent;
    private Object[] indexedArgs;
    private Map<String, Object> mappedArgs;

    @NotNull
    @Contract("_ -> this")
    public EvalUser id(@NotNull String id) {
        this.id = id;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public EvalUser subject(@Nullable Object subject) {
        this.subject = subject;
        return this;
    }

    @Contract("_ -> this")
    public EvalUser agent(@Nullable Object agent) {
        this.agent = agent;
        return this;
    }

    @Contract("_ -> this")
    public EvalUser args(@Nullable Object[] args) {
        this.indexedArgs = args;
        return this;
    }

    @Contract("_ -> this")
    public EvalUser args(@Nullable Map<String, Object> args) {
        this.mappedArgs = args;
        return this;
    }

    @Nullable
    public String getId() {
        if (id != null) {
            return id;
        }
        return Platform.getInstance().getUserId(getAgent());
    }

    @Nullable
    public Object getSubject() {
        return subject;
    }

    @Nullable
    public Object getAgent() {
        return agent != null ? agent : subject;
    }

    @Nullable
    public Object[] getIndexedArgs() {
        return indexedArgs;
    }

    @Nullable
    public Map<String, Object> getMappedArgs() {
        return mappedArgs;
    }

    @NotNull
    public String parse(@Nullable String s) {
        return parse(s, false);
    }

    @NotNull
    public String parse(@Nullable String s, boolean color) {
        return parse(s, (text) -> {
            text.parseAgent(subject, agent);
            if (indexedArgs != null) {
                text.args(indexedArgs);
            }
            if (mappedArgs != null) {
                text.args(mappedArgs);
            }
            if (color) {
                text.color();
            }
        });
    }

    @NotNull
    public String parse(@Nullable String s, @NotNull Consumer<Text> consumer) {
        return s == null ? "null" : Text.of(s, consumer);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public Boolean parseBoolean(@Nullable String s, @Nullable Boolean def) {
        switch (parse(s).toLowerCase()) {
            case "true":
            case "yes":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
                return false;
            default:
                return def;
        }
    }

    @Nullable
    @Contract("_, !null -> !null")
    public Integer parseInt(@Nullable String s, @Nullable Integer def) {
        return parseBy(Integer::parseInt, s, def);
    }

    @Nullable
    @Contract("_, _, !null -> !null")
    public <T> T parseBy(@NotNull Function<String, T> mapper, @Nullable String s, @Nullable T def) {
        try {
            return mapper.apply(parse(s));
        } catch (Exception e) {
            return def;
        }
    }
}
