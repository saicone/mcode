package com.saicone.mcode.bungee;

import com.saicone.mcode.Platform;
import com.saicone.mcode.util.MStrings;

public class BungeePlatform extends Platform {

    public BungeePlatform() {
        setInstance(this);
        MStrings.BUNGEE_HEX = true;
    }
}
