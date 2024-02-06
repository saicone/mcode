package com.saicone.mcode.module.script.action;

import com.saicone.mcode.Platform;
import com.saicone.mcode.module.lang.Display;
import com.saicone.mcode.module.lang.Displays;
import com.saicone.mcode.module.script.Action;
import com.saicone.mcode.module.script.EvalUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BroadcastDisplay extends Action {

    public static final Builder<BroadcastDisplay> BUILDER = new Builder<>("(?i)broadcast(-?display)?") {
        @Override
        public @Nullable BroadcastDisplay build(@Nullable Object object) {
            final Display<Object> display = Displays.loadOrNull(object);
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
    public void accept(@NotNull EvalUser user) {
        display.sendTo(Platform.getInstance().getOnlinePlayers(), s -> user.parse(s, true));
    }
}
