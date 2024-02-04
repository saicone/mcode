package com.saicone.mcode.bukkit.util;

import com.saicone.mcode.util.EasyLookup;

public class BukkitLookup extends EasyLookup {

    static {
        try {
            // Minecraft Server
            addNMSClass("nbt.NBTBase");
            addNMSClass("nbt.NBTTagByte");
            addNMSClass("nbt.NBTTagByteArray");
            addNMSClass("nbt.NBTTagCompound");
            addNMSClass("nbt.NBTTagDouble");
            addNMSClass("nbt.NBTTagFloat");
            addNMSClass("nbt.NBTTagInt");
            addNMSClass("nbt.NBTTagIntArray");
            addNMSClass("nbt.NBTTagList");
            addNMSClass("nbt.NBTTagLong");
            if (ServerInstance.verNumber >= 12) {
                addNMSClass("nbt.NBTTagLongArray");
            }
            addNMSClass("nbt.NBTTagShort");
            addNMSClass("nbt.NBTTagString");
            addNMSClass("nbt.NBTCompressedStreamTools");
            addNMSClass("world.item.ItemStack");
            addNMSClass("world.entity.Entity");
            addNMSClass("world.level.block.entity.TileEntity");
            addNMSClass("world.level.block.state.IBlockData");
            addNMSClass("core.BlockPosition");
            addNMSClass("world.level.World");
            addNMSClass("server.level.WorldServer");
            // Bukkit Server
            addOBCClass("CraftServer");
            addOBCClass("inventory.CraftItemStack");
            addOBCClass("entity.CraftEntity");
            addOBCClass("block.CraftBlockState");
            addOBCClass("CraftWorld");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    BukkitLookup() {
        super();
    }

    /**
     * Save the typically net.minecraft.server class into memory.<br>
     * For +1.17 servers compatibility the name must be the full class path
     * after "net.minecraft."
     *
     * @param name Class name.
     * @return     Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addNMSClass(String name) throws ClassNotFoundException {
        return EasyLookup.addClass(nmsClass(name));
    }

    /**
     * Save the typically net.minecraft.server class into memory with specified ID.<br>
     * For +1.17 servers compatibility the name must be the full class path
     * after "net.minecraft."
     *
     * @param id   Class ID.
     * @param name Class name.
     * @return     Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addNMSClass(String id, String name) throws ClassNotFoundException {
        return EasyLookup.addClass(id, nmsClass(name));
    }

    private static String nmsClass(String name) {
        if (ServerInstance.isUniversal) {
            return "net.minecraft." + name;
        } else {
            return "net.minecraft.server." + ServerInstance.version + "." + (name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name);
        }
    }

    /**
     * Save the typically org.bukkit.craftbukkit class into memory.<br>
     * Name must be the full path after "org.bukkit.craftbukkit.{@link ServerInstance#version}."
     *
     * @param name Class name.
     * @return     Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addOBCClass(String name) throws ClassNotFoundException {
        return EasyLookup.addClass(obcClass(name));
    }

    /**
     * Save the typically org.bukkit.craftbukkit class into memory with specified ID.<br>
     * Name must be the full path after "org.bukkit.craftbukkit.{@link ServerInstance#version}."
     *
     * @param id   Class ID.
     * @param name Class name.
     * @return     Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addOBCClass(String id, String name) throws ClassNotFoundException {
        return EasyLookup.addClass(id, obcClass(name));
    }

    private static String obcClass(String name) {
        return "org.bukkit.craftbukkit." + ServerInstance.version + "." + name;
    }
}
