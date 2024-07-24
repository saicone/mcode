package com.saicone.mcode.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * Server instance class to get information about current server.
 *
 * @author Rubenicos
 */
public class ServerInstance {

    ServerInstance() {
    }

    /**
     * Server type subclass with major changes in compiled instance.
     */
    public static class Type {
        /**
         * Return true if server instance is mojang mapped.
         */
        public static final boolean MOJANG_MAPPED;
        /**
         * Return true if server instance has craftbukkit package relocated.
         */
        public static final boolean CRAFTBUKKIT_RELOCATED;

        static {
            boolean mojangMapped = false;
            try {
                Class.forName("net.minecraft.nbt.CompoundTag");
                mojangMapped = true;
            } catch (ClassNotFoundException ignored) { }
            MOJANG_MAPPED = mojangMapped;

            final String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
            CRAFTBUKKIT_RELOCATED = serverPackage.startsWith("org.bukkit.craftbukkit.v1_");
        }
    }

    /**
     * Server platform subclass with different supported platforms.
     */
    public static class Platform {
        /**
         * Return true if server instance is a SpigotMC server.<br>
         * <a href="https://www.spigotmc.org/">SpigotMC.org</a>
         */
        public static final boolean SPIGOT;
        /**
         * Return true if server instance is a PaperMC server.<br>
         * <a href="https://papermc.io/software/paper">PaperMC.io</a>
         */
        public static final boolean PAPER;
        /**
         * Return true if server instance is a Folia server.<br>
         * <a href="https://papermc.io/software/folia">PaperMC.io</a>
         */
        public static final boolean FOLIA;
        /**
         * Return true if server instance is a PurpurMC server.<br>
         * <a href="https://purpurmc.org/">PurpurMC.org</a>
         */
        public static final boolean PURPUR;

        static {
            boolean spigot = false;
            boolean paper = false;
            boolean folia = false;
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
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                folia = true;
            } catch (ClassNotFoundException ignored) { }
            try {
                ItemStack.class.getDeclaredMethod("hasLore");
                purpur = true;
            } catch (NoSuchMethodException ignored) { }
            SPIGOT = spigot;
            PAPER = paper;
            FOLIA = folia;
            PURPUR = purpur;
        }
    }
}
