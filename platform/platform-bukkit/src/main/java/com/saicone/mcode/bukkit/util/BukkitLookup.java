package com.saicone.mcode.bukkit.util;

import com.saicone.mcode.util.EasyLookup;
import com.saicone.mcode.platform.MC;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class BukkitLookup extends EasyLookup {

    private static final String nmsPackage = MC.version().isUniversal() ? "net.minecraft." : ("net.minecraft.server." + MC.version().bukkitPackage() + ".");
    private static final String obcPackage = Bukkit.getServer().getClass().getPackage().getName() + ".";

    static {
        try {
            // NBT
            addNMSClass("nbt.NBTBase", "Tag");
            addNMSClass("nbt.NBTTagByte", "ByteTag");
            addNMSClass("nbt.NBTTagByteArray", "ByteArrayTag");
            addNMSClass("nbt.NBTTagCompound", "CompoundTag");
            addNMSClass("nbt.NBTTagDouble", "DoubleTag");
            addNMSClass("nbt.NBTTagFloat", "FloatTag");
            addNMSClass("nbt.NBTTagInt", "IntTag");
            addNMSClass("nbt.NBTTagIntArray", "IntArrayTag");
            addNMSClass("nbt.NBTTagList", "ListTag");
            addNMSClass("nbt.NBTTagLong", "LongTag");
            if (MC.version().isNewerThanOrEquals(MC.V_1_12)) {
                addNMSClass("nbt.NBTTagLongArray", "LongArrayTag");
            }
            addNMSClass("nbt.NBTTagShort", "ShortTag");
            addNMSClass("nbt.NBTTagString", "StringTag");
            addNMSClass("nbt.NBTCompressedStreamTools", "NbtIo");
            addNMSClass("nbt.NBTReadLimiter", "NbtAccounter");
            addNMSClass("nbt.MojangsonParser", "TagParser");
            if (MC.version().isFlat()) {
                addNMSClass("nbt.DynamicOpsNBT", "NbtOps");
            }

            // DataComponent
            if (MC.version().isComponent()) {
                addNMSClass("core.component.DataComponentHolder");
                addNMSClass("core.component.DataComponentMap");
                addNMSClassId("DataComponentMap.Builder", "core.component.DataComponentMap$a", "core.component.DataComponentMap$Builder");
                addNMSClassId("DataComponentMap.SimpleMap", "core.component.DataComponentMap$a$a", "core.component.DataComponentMap$Builder$SimpleMap");
                addNMSClass("core.component.DataComponentPatch");
                addNMSClassId("DataComponentPatch.Builder", "core.component.DataComponentPatch$a", "core.component.DataComponentPatch$Builder");
                addNMSClass("core.component.PatchedDataComponentMap");
                addNMSClass("core.component.TypedDataComponent");
                addNMSClass("core.component.DataComponentType");
            }
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
     * @param name    Class name.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    @NotNull
    public static Class<?> addNMSClass(@NotNull String name) throws ClassNotFoundException {
        return addNMSClass(name, new String[0]);
    }

    /**
     * Save the typically net.minecraft.server class into memory.<br>
     * For +1.17 servers compatibility the name must be the full class path
     * after "net.minecraft."
     *
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    @NotNull
    public static Class<?> addNMSClass(@NotNull String name, @NotNull String... aliases) throws ClassNotFoundException {
        return addClass(nmsClass(name, aliases), aliases);
    }

    /**
     * Save the typically net.minecraft.server class into memory with specified ID.<br>
     * For +1.17 servers compatibility the name must be the full class path
     * after "net.minecraft."
     *
     * @param id      Class ID.
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    @NotNull
    public static Class<?> addNMSClassId(@NotNull String id, @NotNull String name, @NotNull String... aliases) throws ClassNotFoundException {
        return addClassId(id, nmsClass(name, aliases), aliases);
    }

    @NotNull
    private static String nmsClass(@NotNull String name, @NotNull String... aliases) {
        for (int i = 0; i < aliases.length; i++) {
            final String alias = aliases[i];
            if (alias.contains(".")) {
                aliases[i] = nmsPackage + alias;
            }
        }
        if (MC.version().isUniversal()) {
            return nmsPackage + name;
        } else {
            return nmsPackage + (name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name);
        }
    }

    /**
     * Save the typically org.bukkit.craftbukkit class into memory.<br>
     * Name must be the full path after craftbukkit package.
     *
     * @param name    Class name.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    @NotNull
    public static Class<?> addOBCClass(@NotNull String name) throws ClassNotFoundException {
        return addOBCClass(name, new String[0]);
    }

    /**
     * Save the typically org.bukkit.craftbukkit class into memory.<br>
     * Name must be the full path after craftbukkit package.
     *
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    @NotNull
    public static Class<?> addOBCClass(@NotNull String name, @NotNull String... aliases) throws ClassNotFoundException {
        return addClass(obcClass(name, aliases), aliases);
    }

    /**
     * Save the typically org.bukkit.craftbukkit class into memory with specified ID.<br>
     * Name must be the full path after craftbukkit package.
     *
     * @param id      Class ID.
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    @NotNull
    public static Class<?> addOBCClassId(@NotNull String id, @NotNull String name, @NotNull String... aliases) throws ClassNotFoundException {
        return addClassId(id, obcClass(name, aliases), aliases);
    }

    @NotNull
    private static String obcClass(@NotNull String name, @NotNull String... aliases) {
        for (int i = 0; i < aliases.length; i++) {
            final String alias = aliases[i];
            if (alias.contains(".")) {
                aliases[i] = obcPackage + alias;
            }
        }
        return obcPackage + name;
    }
}
