package com.saicone.mcode.module.script;

import com.saicone.mcode.module.script.action.BroadcastDisplay;
import com.saicone.mcode.module.script.action.Delay;
import com.saicone.mcode.module.script.action.SendDisplay;
import com.saicone.mcode.module.script.condition.Compare;
import com.saicone.mcode.module.script.condition.Cooldown;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Script {

    public static final ScriptRegistry REGISTRY = new ScriptRegistry();

    static {
        Delay.BUILDER.register();
        try {
            Class.forName("com.saicone.mcode.module.lang.Displays");
            SendDisplay.BUILDER.register();
            BroadcastDisplay.BUILDER.register();
        } catch (ClassNotFoundException ignored) { }
        REGISTRY.putCondition(EvalKey.regex("(?i)compare|eval"), object -> new Compare(String.valueOf(object)));
        try {
            Class.forName("com.saicone.mcode.util.cache.Cache");
            REGISTRY.putCondition(EvalKey.regex("(?i)cooldown"), object -> new Cooldown(String.valueOf(object)));
        } catch (ClassNotFoundException ignored) { }
    }

    private final ScriptCompiler compiler;

    private Object loaded;
    private ScriptFunction<EvalUser, ?> compiled;

    public Script() {
        this(ScriptCompiler.compiler());
    }

    public Script(@NotNull ScriptCompiler compiler) {
        this.compiler = compiler;
    }

    @NotNull
    public ScriptCompiler getCompiler() {
        return compiler;
    }

    public Object getLoaded() {
        return loaded;
    }

    public ScriptFunction<EvalUser, ?> getCompiled() {
        return compiled;
    }

    @Nullable
    @Contract("_, !null -> !null")
    @SuppressWarnings("unchecked")
    public <T> T apply(@NotNull EvalUser user, @Nullable T def) {
        if (compiled == null) {
            return def;
        }
        final Object result = compiled.apply(user);
        if (result == null) {
            return def;
        }
        try {
            return (T) result;
        } catch (ClassCastException e) {
            return def;
        }
    }

    @NotNull
    public ActionResult run(@NotNull EvalUser user) {
        return run(user, ActionResult.DONE);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public ActionResult run(@NotNull EvalUser user, @Nullable ActionResult def) {
        return apply(user, def);
    }

    public boolean eval(@NotNull EvalUser user) {
        return eval(user, false);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public Boolean eval(@NotNull EvalUser user, @Nullable Boolean def) {
        return apply(user, def);
    }

    public void load(@Nullable Object loaded, @Nullable ScriptFunction<EvalUser, ?> compiled) {
        this.loaded = loaded;
        this.compiled = compiled;
    }

    public void loadAction(@Nullable Object object) {
        load(object, compiler.compileAction(object));
    }

    public void loadAction(@Nullable Object action, @Nullable Object object) {
        load(action, compiler.compileAction(action, object));
    }

    public void loadCondition(@Nullable Object condition) {
        load(condition, compiler.compileCondition(condition));
    }

    public void loadCondition(@Nullable Object condition, @Nullable Object object) {
        load(condition, compiler.compileCondition(condition, object));
    }

    @Override
    public String toString() {
        return String.valueOf(loaded);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Script script = (Script) o;

        return Objects.equals(loaded, script.loaded);
    }

    @Override
    public int hashCode() {
        return loaded != null ? loaded.hashCode() : 0;
    }
}
