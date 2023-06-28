package com.saicone.mcode.bukkit.util;

import org.bukkit.Bukkit;

/**
 * Server instance class to get information about current server.
 *
 * @author Rubenicos
 */
public class ServerInstance {

    /**
     * Current server version defined in craftbukkit package.
     */
    public static final String version;
    /**
     * Current server version number simplified, for example:<br>
     * 1.8 -&gt; 8<br>
     * 1.12.2 -&gt; 12<br>
     * 1.17 -&gt; 17
     */
    public static final int verNumber;
    /**
     * Current release version number, for example:<br>
     * v1_9_R2 -&gt; 2<br>
     * v1_13_R1 -&gt; 1<br>
     * v1_16_R3 -&gt; 3<br>
     */
    public static final int release;

    /**
     * Return true if server version is 1.12.2 or below.
     */
    public static final boolean isLegacy;
    /**
     * Return true if server version is 1.13 or upper.
     */
    public static final boolean isFlat;
    /**
     * Return true if server version is 1.17 or upper.
     */
    public static final boolean isUniversal;
    /**
     * Return true if server instance is a SpigotMC server.<br>
     * <a href="https://www.spigotmc.org/">https://www.spigotmc.org/</a>
     */
    public static final boolean isSpigot;
    /**
     * Return true if server instance is a PaperMC server.<br>
     * <a href="https://papermc.io/">https://papermc.io/</a>
     */
    public static final boolean isPaper;
    /**
     * Return true if server instance is a PurpurMC server.<br>
     * <a href="https://purpur.org/">https://purpur.org/</a>
     */
    public static final boolean isPurpur;

    static {
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        verNumber = Integer.parseInt(version.split("_")[1]);
        release = Integer.parseInt(version.split("_")[2].substring(1));
        isLegacy = verNumber <= 12;
        isFlat = verNumber >= 13;
        isUniversal = verNumber >= 17;
        boolean spigot = false;
        boolean paper = false;
        boolean purpur = false;
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            spigot = true;
        } catch (ClassNotFoundException ignored) { }
        try {
            Class.forName("com.destroystokyo.paper.Title");
            paper = true;
        } catch (ClassNotFoundException ignored) { }
        try {
            Class.forName("org.purpurmc.purpur.event.ExecuteCommandEvent");
            purpur = true;
        } catch (ClassNotFoundException ignored) { }
        isSpigot = spigot;
        isPaper = paper;
        isPurpur = purpur;
    }

    ServerInstance() {
    }
}
