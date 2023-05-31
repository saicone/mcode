package com.saicone.mcode.module.script.action;

import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.module.lang.display.Display;
import com.saicone.mcode.module.script.Action;
import com.saicone.mcode.module.script.ActionResult;
import com.saicone.mcode.module.script.EvalUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BroadcastDisplay extends Action {

    public static final Builder<BroadcastDisplay> BUILDER = new Builder<>("(?i)broadcast(-?display)?") {
        @Override
        public @Nullable BroadcastDisplay build(@Nullable Object object) {
            final Display<Object> display = DisplayLoader.loadDisplay(object);
            if (display == null) {
                return null;
            }
            return new BroadcastDisplay(display);
        }
    };

    private final Display<Object> display;

    public BroadcastDisplay(@NotNull Display<Object> display) {
        this.display = display;
    }

    @NotNull
    public Display<Object> getDisplay() {
        return display;
    }

    @Override
    public @NotNull <T extends EvalUser> ActionResult run(@NotNull T user) {
        display.sendToAll(user::parse);
        return ActionResult.DONE;
    }
}