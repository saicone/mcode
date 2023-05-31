package com.saicone.mcode.module.script.action;

import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.module.lang.display.Display;
import com.saicone.mcode.module.script.Action;
import com.saicone.mcode.module.script.ActionResult;
import com.saicone.mcode.module.script.EvalUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SendDisplay extends Action {

    public static final Builder<SendDisplay> BUILDER = new Builder<>("(?i)(send-?)?display") {
        @Override
        public @Nullable SendDisplay build(@Nullable Object object) {
            final Display<Object> display = DisplayLoader.loadDisplay(object);
            if (display == null) {
                return null;
            }
            return new SendDisplay(display);
        }
    };

    private final Display<Object> display;

    public SendDisplay(@NotNull Display<Object> display) {
        this.display = display;
    }

    @NotNull
    public Display<Object> getDisplay() {
        return display;
    }

    @Override
    public @NotNull <T extends EvalUser> ActionResult run(@NotNull T user) {
        try {
            if (user.getSubject() != null) {
                display.sendTo(user.getSubject(), user::parse);
            }
        } catch (ClassCastException ignored) { }
        return ActionResult.DONE;
    }
}