package com.saicone.mcode.env;

public enum Executes {

    /**
     * Executed when bootstrap is initialized.
     */
    BOOT,
    /**
     * Executed when plugin instance is created.
     */
    INIT,
    /**
     * Executed when plugin is loaded.
     */
    LOAD,
    /**
     * Executed when plugin is enabled.
     */
    ENABLE,
    /**
     * Executed when plugin is reloaded.
     */
    RELOAD,
    /**
     * Executed when plugin is disabled.
     */
    DISABLE;

}
