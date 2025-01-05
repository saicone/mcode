package com.saicone.mcode.bukkit.util;

import com.saicone.mcode.util.invoke.EasyLookup;
import com.saicone.mcode.platform.MC;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

public class BukkitLookup extends EasyLookup {

    private static final String MINECRAFT = MC.version().isUniversal() ? "net.minecraft." : ("net.minecraft.server." + MC.version().bukkitPackage() + ".");
    private static final String BUKKIT = Bukkit.getServer().getClass().getPackage().getName() + ".";

    static {
        try {
            // NBT
            addMinecraftClass("nbt.Tag", "NBTBase");
            addMinecraftClass("nbt.ByteTag", "NBTTagByte");
            addMinecraftClass("nbt.ByteArrayTag", "NBTTagByteArray");
            addMinecraftClass("nbt.CompoundTag", "NBTTagCompound");
            addMinecraftClass("nbt.DoubleTag", "NBTTagDouble");
            addMinecraftClass("nbt.EndTag", "NBTTagEnd");
            addMinecraftClass("nbt.FloatTag", "NBTTagFloat");
            addMinecraftClass("nbt.IntTag", "NBTTagInt");
            addMinecraftClass("nbt.IntArrayTag", "NBTTagIntArray");
            addMinecraftClass("nbt.ListTag", "NBTTagList");
            addMinecraftClass("nbt.LongTag", "NBTTagLong");
            if (MC.version().isNewerThanOrEquals(MC.V_1_12)) {
                addMinecraftClass("nbt.LongArrayTag", "NBTTagLongArray");
            }
            addMinecraftClass("nbt.ShortTag", "NBTTagShort");
            addMinecraftClass("nbt.StringTag", "NBTTagString");
            addMinecraftClass("nbt.NbtIo", "NBTCompressedStreamTools");
            addMinecraftClass("nbt.NbtAccounter", "NBTReadLimiter");
            addMinecraftClass("nbt.TagParser", "MojangsonParser");
            if (MC.version().isFlat()) {
                addMinecraftClass("nbt.NbtOps", "DynamicOpsNBT");
            }

            // DataComponent
            if (MC.version().isComponent()) {
                addMinecraftClass("core.component.DataComponentHolder");
                addMinecraftClass("core.component.DataComponentMap");
                addMinecraftClassId("DataComponentMap.Builder", "core.component.DataComponentMap$Builder", "core.component.DataComponentMap$a");
                addMinecraftClassId("DataComponentMap.SimpleMap", "core.component.DataComponentMap$Builder$SimpleMap", "core.component.DataComponentMap$a$a");
                addMinecraftClass("core.component.DataComponentPatch");
                addMinecraftClassId("DataComponentPatch.Builder", "core.component.DataComponentPatch$Builder", "core.component.DataComponentPatch$a");
                addMinecraftClass("core.component.PatchedDataComponentMap");
                addMinecraftClass("core.component.TypedDataComponent");
                addMinecraftClass("core.component.DataComponentType");
                addMinecraftClassId("HolderLookup.Provider", "core.HolderLookup$Provider", "core.HolderLookup$a");
            } else if (MC.version().isNewerThanOrEquals(MC.V_1_19_3)) {
                addMinecraftClassId("HolderLookup.Provider", "core.HolderLookup$Provider", "core.HolderLookup$b");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    BukkitLookup() {
        super();
    }

    /**
     * Find field, constructor or method declaration from provided class by
     * providing an unmapped name to use if server instance is not using mojang mappings.
     *
     * @see #setter(Object, Object, String)
     * @see #staticSetter(Object, Object, String)
     * @see #getter(Object, Object, String)
     * @see #staticGetter(Object, Object, String)
     * @see #constructor(Object, Object...)
     * @see #method(Object, Object, String, Object...)
     * @see #staticMethod(Object, Object, String, Object...)
     *
     * @param clazz        Class to find into.
     * @param any          The value to find.
     * @param unmappedName Supplier that return the unmapped name.
     * @return             The reflected value that was found as MethodHandle, null otherwise.
     */
    public static MethodHandle find(@NotNull Object clazz, @NotNull String any, @NotNull Supplier<String> unmappedName) {
        return find(clazz, () -> any, unmappedName);
    }

    /**
     * Find supplied field, constructor or method declaration from provided class by
     * providing an unmapped name to use if server instance is not using mojang mappings.
     *
     * @see #setter(Object, Object, String)
     * @see #staticSetter(Object, Object, String)
     * @see #getter(Object, Object, String)
     * @see #staticGetter(Object, Object, String)
     * @see #constructor(Object, Object...)
     * @see #method(Object, Object, String, Object...)
     * @see #staticMethod(Object, Object, String, Object...)
     *
     * @param clazz        Class to find into.
     * @param any          Supplier that return the value to find.
     * @param unmappedName Supplier that return the unmapped name.
     * @return             The reflected value that was found as MethodHandle, null otherwise.
     */
    public static MethodHandle find(@NotNull Object clazz, @NotNull Supplier<String> any, @NotNull Supplier<String> unmappedName) {
        return find(clazz, () -> {
            final String s = any.get();
            if (s == null) {
                return null;
            }
            if (ServerInstance.Type.MOJANG_MAPPED) {
                return s;
            }
            final int bracket = s.indexOf('(');
            if (bracket == 0) { // Constructor
                return s;
            } else if (bracket > 0) { // Method
                final String s1 = s.substring(0, bracket);
                final int space = s1.lastIndexOf(' ');
                if (space < 0) {
                    return unmappedName.get() + s.substring(bracket);
                } else {
                    return s1.substring(0, space) + " " + unmappedName.get() + s.substring(bracket);
                }
            } else { // Field
                final int space = s.lastIndexOf(' ');
                if (space < 0) {
                    return unmappedName.get();
                } else {
                    return s.substring(0, space) + " " + unmappedName.get();
                }
            }
        });
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
    public static Class<?> addMinecraftClass(@NotNull String name) throws ClassNotFoundException {
        return addMinecraftClass(name, new String[0]);
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
    public static Class<?> addMinecraftClass(@NotNull String name, @NotNull String... aliases) throws ClassNotFoundException {
        return addClass(minecraftClass(name, aliases), aliases);
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
    public static Class<?> addMinecraftClassId(@NotNull String id, @NotNull String name, @NotNull String... aliases) throws ClassNotFoundException {
        return addClassId(id, minecraftClass(name, aliases), aliases);
    }

    @NotNull
    private static String minecraftClass(@NotNull String name, @NotNull String... aliases) {
        for (int i = 0; i < aliases.length; i++) {
            final String alias = aliases[i];
            if (alias.contains(".")) {
                aliases[i] = MINECRAFT + alias;
            }
        }
        if (MC.version().isUniversal()) {
            return MINECRAFT + name;
        } else {
            return MINECRAFT + (name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name);
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
    public static Class<?> addBukkitClass(@NotNull String name) throws ClassNotFoundException {
        return addBukkitClass(name, new String[0]);
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
    public static Class<?> addBukkitClass(@NotNull String name, @NotNull String... aliases) throws ClassNotFoundException {
        return addClass(bukkitClass(name, aliases), aliases);
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
    public static Class<?> addBukkitClassId(@NotNull String id, @NotNull String name, @NotNull String... aliases) throws ClassNotFoundException {
        return addClassId(id, bukkitClass(name, aliases), aliases);
    }

    @NotNull
    private static String bukkitClass(@NotNull String name, @NotNull String... aliases) {
        for (int i = 0; i < aliases.length; i++) {
            final String alias = aliases[i];
            if (alias.contains(".")) {
                aliases[i] = BUKKIT + alias;
            }
        }
        return BUKKIT + name;
    }
}
