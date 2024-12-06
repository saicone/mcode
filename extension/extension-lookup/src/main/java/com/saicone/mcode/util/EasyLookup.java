package com.saicone.mcode.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Class to handle reflection lookups in an easy way.
 *
 * @author Rubenicos
 */
public class EasyLookup {

    private static final boolean DEBUG = "true".equals(System.getProperty("saicone.easylookup.debug"));
    private static final Logger LOGGER = Logger.getLogger("EasyLookup");

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Class<?>, MethodHandles.Lookup> PRIVATE_LOOKUPS = new HashMap<>();
    private static final Map<String, Class<?>> CLASS_ID_MAP = new HashMap<>();
    private static final MethodPredicate[] METHOD_PREDICATES = new MethodPredicate[] {
            (m, type, params) -> m.getReturnType().equals(type) && Arrays.equals(m.getParameterTypes(), params),
            (m, type, params) -> type.isAssignableFrom(m.getReturnType()) && Arrays.equals(m.getParameterTypes(), params),
            (m, type, params) -> m.getReturnType().equals(type) && isAssignableFrom(params, m.getParameterTypes()),
            (m, type, params) -> type.isAssignableFrom(m.getReturnType()) && isAssignableFrom(params, m.getParameterTypes()),
            (m, type, params) -> Arrays.equals(m.getParameterTypes(), params),
            (m, type, params) -> isAssignableFrom(params, m.getParameterTypes())
    };

    static {
        try {
            // Primitive
            addClassId("void", void.class);
            addClassId("Character", Character.class);
            addClassId("char", char.class);
            addClassId("char[]", "[C");
            addClassId("Boolean", Boolean.class);
            addClassId("boolean", boolean.class);
            addClassId("boolean[]", "[Z");
            addClassId("Byte", Byte.class);
            addClassId("byte", byte.class);
            addClassId("byte[]", "[B");
            addClassId("Short", Short.class);
            addClassId("short", short.class);
            addClassId("short[]", "[S");
            addClassId("Integer", Integer.class);
            addClassId("int", int.class);
            addClassId("int[]", "[I");
            addClassId("Long", Long.class);
            addClassId("long", long.class);
            addClassId("long[]", "[J");
            addClassId("Float", Float.class);
            addClassId("float", float.class);
            addClassId("float[]", "[F");
            addClassId("Double", Double.class);
            addClassId("double", double.class);
            addClassId("double[]", "[D");
            // Java
            addClassId("Class", Class.class);
            addClassId("Object", Object.class);
            addClassId("Number", Number.class);
            addClassId("String", String.class);
            addClassId("BigInteger", BigInteger.class);
            addClassId("BigDecimal", BigDecimal.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected EasyLookup() {
    }

    @Nullable
    public static MethodHandle find(@NotNull Object clazz, @NotNull String any) {
        return find(clazz, () -> any);
    }

    @Nullable
    public static MethodHandle find(@NotNull Object clazz, @NotNull Supplier<String> any) {
        try {
            return unsafeFind(clazz, any);
        } catch (Throwable t) {
            return null;
        }
    }

    @Nullable
    public static MethodHandle unsafeFind(@NotNull Object clazz, @NotNull Supplier<String> any) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        final String s = any.get();
        if (s == null) {
            return null;
        }
        final int bracket = s.trim().indexOf('(');
        if (bracket < 0) { // Field
            final int space = s.lastIndexOf(' ');
            final String modifier = s.substring(0, space);
            final String type = modifier.substring(modifier.lastIndexOf(' ') + 1);
            final String name = s.substring(space + 1);
            if (modifier.contains("set")) {
                if (modifier.contains("static")) {
                    return staticSetter(clazz, type, name);
                } else {
                    return setter(clazz, type, name);
                }
            } else {
                if (modifier.contains("static")) {
                    return staticGetter(clazz, type, name);
                } else {
                    return getter(clazz, type, name);
                }
            }
        }

        final Object[] parameterTypes;
        if (bracket + 1 >= s.length()) {
            parameterTypes = new String[0];
        } else {
            parameterTypes = Arrays.stream(s.substring(bracket + 1, s.length() - 1).split(","))
                    .map(parameter -> {
                        final int space = parameter.lastIndexOf(' ');
                        if (space > 0) {
                            return parameter.substring(0, space);
                        }
                        return parameter;
                    })
                    .toArray();
        }
        if (bracket == 0) { // Constructor
            return constructor(clazz, parameterTypes);
        }

        // Method
        final String s1 = s.substring(0, bracket);
        final int space = s1.lastIndexOf(' ');
        final String modifier = s1.substring(0, space);
        final String type = modifier.substring(modifier.lastIndexOf(' ') + 1);
        final String name = s1.substring(space + 1);
        if (modifier.contains("static")) {
            return staticMethod(clazz, type, name, parameterTypes);
        } else {
            return method(clazz, type, name, parameterTypes);
        }
    }

    /**
     * Test the availability of provided class name using {@link Class#forName(String)}.
     *
     * @param name Class name.
     * @return     true if the provided class exists.
     */
    public static boolean testClass(@NotNull String name) {
        boolean test = false;
        try {
            Class.forName(name);
            test = true;
        } catch (Throwable ignored) { }
        return test;
    }

    /**
     * Get previously saved class by it ID.
     *
     * @param id Class ID.
     * @return   A class represented by provided ID or null.
     */
    @NotNull
    public static Class<?> classById(@NotNull String id) {
        final Class<?> clazz = CLASS_ID_MAP.get(id);
        if (clazz == null) {
            if (id.endsWith("[]")) {
                final Class<?> nonArray = classById(id.substring(0, id.length() - 2));
                try {
                    return Class.forName("[" + nonArray);
                } catch (ClassNotFoundException ignored) { }
            }
            throw new IllegalArgumentException("The class with ID '" + id + "' doesn't exist");
        }
        return clazz;
    }

    /**
     * Get class represented by Object.<br>
     * If object is instance of Class will return itself,
     * otherwise return {@link #classById(String)}.
     *
     * @param object Class or String.
     * @return       A class represented by provided object or null.
     */
    @NotNull
    public static Class<?> classOf(@NotNull Object object) {
        if (object instanceof Class) {
            return (Class<?>) object;
        } else {
            return classById(String.valueOf(object).trim());
        }
    }

    /**
     * Same has {@link #classOf(Object)} but for multiple objects.
     *
     * @param classes Classes objects.
     * @return        An array of classes represented by objects.
     */
    @NotNull
    public static Class<?>[] classesOf(@NotNull Object... classes) {
        Class<?>[] array = new Class<?>[classes.length];
        for (int i = 0; i < classes.length; i++) {
            array[i] = classOf(classes[i]);
        }
        return array;
    }

    /**
     * Same has {@link Class#forName(String)} but save the class into memory.
     *
     * @param name    Class name.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    @NotNull
    public static Class<?> addClass(@NotNull String name) throws ClassNotFoundException {
        return addClass(name, new String[0]);
    }

    /**
     * Same has {@link Class#forName(String)} but save the class into memory.
     *
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    @NotNull
    public static Class<?> addClass(@NotNull String name, @NotNull String... aliases) throws ClassNotFoundException {
        return addClassId(name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name, name, aliases);
    }

    /**
     * Same has {@link Class#forName(String)} but save the class into memory
     * with provided ID to get from {@link #classById(String)}.
     *
     * @param id      Class ID.
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    @NotNull
    public static Class<?> addClassId(@NotNull String id, @NotNull String name, @NotNull String... aliases) throws ClassNotFoundException {
        String finalName = null;
        if (testClass(name)) {
            finalName = name;
        } else if (aliases.length > 0) {
            final String pkg = name.contains(".") ? name.substring(0, name.lastIndexOf('.') + 1) : "";
            for (String alias : aliases) {
                final String aliasName = (alias.contains(".") ? "" : pkg) + alias;
                if (testClass(aliasName)) {
                    finalName = aliasName;
                    break;
                }
            }
        }

        if (finalName == null) {
            throw new ClassNotFoundException(name);
        }
        return addClassId(id, Class.forName(finalName));
    }

    /**
     * Save class into memory with provided ID to get from {@link #classById(String)}.
     *
     * @param id    Class ID.
     * @param clazz Class object.
     * @return      Added class.
     */
    @NotNull
    public static Class<?> addClassId(@NotNull String id, @NotNull Class<?> clazz) {
        if (DEBUG) {
            final Class<?> value = CLASS_ID_MAP.get(id);
            if (value != null && !value.equals(clazz)) {
                LOGGER.info("Replacing class ID: '" + id + "' [old = " + value.getName() + ", new = " + clazz.getName() + "]");
            }
        }
        CLASS_ID_MAP.put(id, clazz);
        return clazz;
    }

    /**
     * Same has {@link MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)} but save the result into memory.
     *
     * @param clazz Target class.
     * @return      Private lookup or null.
     */
    @Nullable
    public static MethodHandles.Lookup privateLookup(@NotNull Class<?> clazz) {
        return PRIVATE_LOOKUPS.computeIfAbsent(clazz, c -> {
            try {
                return MethodHandles.privateLookupIn(clazz, LOOKUP);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findConstructor(Class, MethodType)} without
     * creating a MethodType, it also provides void class at first argument has default.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     *
     * See also {@link #unreflectConstructor(Object, Object...)} for private constructors.
     *
     * @param clazz          Class to find constructor.
     * @param parameterTypes Required classes in constructor.
     * @return               A MethodHandle representing class constructor.
     * @throws NoSuchMethodException  if the constructor does not exist.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    @NotNull
    public static MethodHandle constructor(@NotNull Object clazz, @NotNull Object... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        final Class<?> from = classOf(clazz);
        try {
            return LOOKUP.findConstructor(from, type(void.class, parameterTypes));
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                LOGGER.info("unreflectConstructor = '" + from.getName() + '(' + String.join(", ", names(classesOf(parameterTypes))) + ")'");
            }
            return unreflectConstructor(from, parameterTypes);
        } catch (NoSuchMethodException e) {
            if (DEBUG) {
                LOGGER.info("findConstructor = '" + from.getName() + '(' + String.join(", ", names(classesOf(parameterTypes))) + ")'");
            }
            return unreflectConstructor(findConstructor(from, classesOf(parameterTypes)));
        }
    }

    /**
     * Same has {@link MethodHandles.Lookup#unreflectConstructor(Constructor)},
     * but this method makes the constructor accessible if unreflection fails.
     *
     * @param constructor The reflected constructor.
     * @return            A MethodHandle representing constructor.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    @NotNull
    public static MethodHandle unreflectConstructor(@NotNull Constructor<?> constructor) throws IllegalAccessException {
        try {
            return LOOKUP.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            constructor.setAccessible(true);
            return LOOKUP.unreflectConstructor(constructor);
        }
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflectConstructor(Constructor)},
     * this method creates an accessible {@link Constructor} and unreflect
     * it, useful for private constructors.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz          Class to find constructor.
     * @param parameterTypes Required classes in constructor.
     * @return               A MethodHandle representing class constructor.
     * @throws NoSuchMethodException  if a matching method is not found.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    @NotNull
    public static MethodHandle unreflectConstructor(@NotNull Object clazz, @NotNull Object... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        Constructor<?> c = classOf(clazz).getDeclaredConstructor(classesOf(parameterTypes));
        c.setAccessible(true);
        return LOOKUP.unreflectConstructor(c);
    }

    /**
     * Find constructor inside class using recursive searching and
     * invoke {@link MethodHandles.Lookup#unreflectConstructor(Constructor)}.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param from           Class to find constructor.
     * @param parameterTypes Required classes in constructor.
     * @return               A constructor for provided class.
     * @throws NoSuchMethodException  if a matching method is not found.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    @NotNull
    public static Constructor<?> findConstructor(@NotNull Class<?> from, @NotNull Class<?>... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        // Find with reflection
        try {
            return from.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException ignored) { }
        // Find using constructor parameters
        for (Constructor<?> constructor : from.getDeclaredConstructors()) {
            if (isAssignableFrom(parameterTypes, constructor.getParameterTypes())) {
                return constructor;
            }
        }
        throw new NoSuchMethodException("Cannot find a constructor like '" + from.getName() + '(' + String.join(", ", names(parameterTypes)) + ")'");
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findVirtual(Class, String, MethodType)} without
     * creating a MethodType, this method require to specify the return type class of reflected {@link Method}
     * and its only compatible with instance public methods, see {@link #staticMethod(Object, Object, String, Object...)}
     * for static public methods.<br>
     * <p>
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     * <p>
     * See also {@link #unreflectMethod(Object, String, Object...)} for private methods.
     *
     * @param clazz          Class to find public method.
     * @param returnType     Return type class for method.
     * @param name           Method name.
     * @param parameterTypes Required classes in method.
     * @return A MethodHandle representing a instance method for provided class.
     * @throws NoSuchMethodException  if the method does not exist.
     * @throws IllegalAccessException if access checking fails, or if the method is static, or if the method's
     *                                variable arity modifier bit is set and asVarargsCollector fails.
     */
    @NotNull
    public static MethodHandle method(@NotNull Object clazz, @NotNull Object returnType, @NotNull String name, @NotNull Object... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        final Class<?> from = classOf(clazz);
        try {
            return LOOKUP.findVirtual(from, name, type(returnType, parameterTypes));
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                LOGGER.info("unreflectMethod = '" + classOf(returnType).getName() + ' ' + name + '(' + String.join(", ", names(classesOf(parameterTypes))) + ")' inside class " + from.getName());
            }
            return unreflectMethod(from, name, parameterTypes);
        } catch (NoSuchMethodException e) {
            if (DEBUG) {
                LOGGER.info("findMethod = '" + classOf(returnType).getName() + ' ' + name + '(' + String.join(", ", names(classesOf(parameterTypes))) + ")' inside class " + from.getName());
            }
            return unreflectMethod(findMethod(from, false, classOf(returnType), name, classesOf(parameterTypes)));
        }
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflect(Method)},
     * but this method makes the method accessible if unreflection fails.
     *
     * @param method Method to unreflect.
     * @return       A MethodHandle representing a method.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    @NotNull
    public static MethodHandle unreflectMethod(@NotNull Method method) throws IllegalAccessException {
        try {
            return LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            method.setAccessible(true);
            return LOOKUP.unreflect(method);
        }
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflect(Method)},
     * this method creates and accessible {@link Method} and
     * unreflect it, can be static or instance method.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz          Class to find method.
     * @param name           Method name.
     * @param parameterTypes Required classes in method.
     * @return               A MethodHandle representing a method for provided class.
     * @throws NoSuchMethodException  if a matching method is not found or if the name is "&lt;init&gt;"or "&lt;clinit&gt;".
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    @NotNull
    public static MethodHandle unreflectMethod(@NotNull Object clazz, @NotNull String name, @NotNull Object... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        Method m = classOf(clazz).getDeclaredMethod(name, classesOf(parameterTypes));
        m.setAccessible(true);
        return LOOKUP.unreflect(m);
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findStatic(Class, String, MethodType)} without
     * creating a MethodType, this method require to specify the return type class of reflected {@link Method}
     * and only compatible with static methods.<br>
     * <p>
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     * <p>
     * See also {@link #unreflectMethod(Object, String, Object...)} for private methods.
     *
     * @param clazz          Class to find method.
     * @param returnType     Return type class for method.
     * @param name           Method name.
     * @param parameterTypes Required classes in method.
     * @return A MethodHandle representing a static method for provided class.
     * @throws NoSuchMethodException  if the method does not exist.
     * @throws IllegalAccessException if access checking fails, or if the method is not static, or if the method's
     *                                variable arity modifier bit is set and asVarargsCollector fails.
     */
    @NotNull
    public static MethodHandle staticMethod(@NotNull Object clazz, @NotNull Object returnType, @NotNull String name, @NotNull Object... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        final Class<?> from = classOf(clazz);
        try {
            return LOOKUP.findStatic(from, name, type(returnType, parameterTypes));
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                LOGGER.info("unreflectMethod = 'static " + classOf(returnType).getName() + ' ' + name + '(' + String.join(", ", names(classesOf(parameterTypes))) + ")' inside class " + from.getName());
            }
            return unreflectMethod(from, name, parameterTypes);
        } catch (NoSuchMethodException e) {
            if (DEBUG) {
                LOGGER.info("findMethod = 'static " + classOf(returnType).getName() + ' ' + name + '(' + String.join(", ", names(classesOf(parameterTypes))) + ")' inside class " + from.getName());
            }
            return unreflectMethod(findMethod(from, true, classOf(returnType), name, classesOf(parameterTypes)));
        }
    }

    /**
     * Find method inside class using recursirve searching and
     * invoke {@link MethodHandles.Lookup#unreflect(Method)}.<br>
     * <p>
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param from           Class to find method.
     * @param isStatic       True if method is static.
     * @param returnType     Return type class for method.
     * @param name           Method name.
     * @param parameterTypes Required classes in method.
     * @return A method from provided class.
     * @throws NoSuchMethodException  if the method does not exist.
     * @throws IllegalAccessException if access checking fails, or if the method is not static, or if the method's
     *                                variable arity modifier bit is set and asVarargsCollector fails.
     */
    @NotNull
    public static Method findMethod(@NotNull Class<?> from, boolean isStatic, @NotNull Class<?> returnType, @NotNull String name, @NotNull Class<?>... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        // Find with reflection
        try {
            return from.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException ignored) { }
        // Find using method information
        final Method[] declaredMethods = Arrays.stream(from.getDeclaredMethods()).filter(m -> Modifier.isStatic(m.getModifiers()) == isStatic).toArray(Method[]::new);

        Method foundMethod = null;
        for (MethodPredicate predicate : METHOD_PREDICATES) {
            for (Method method : declaredMethods) {
                if (predicate.test(method, returnType, parameterTypes)) {
                    if (method.getName().equals(name)) {
                        return method;
                    } else if (foundMethod == null) {
                        foundMethod = method;
                    }
                }
            }
            if (foundMethod != null) {
                break;
            }
        }
        if (foundMethod != null) {
            return foundMethod;
        }
        throw new NoSuchMethodException("Cannot find a method like '" + (isStatic ? "static " : "") + returnType.getName() + ' ' + name + '(' + String.join(", ", names(parameterTypes)) + ")' inside class " + from.getName());
    }

    /**
     * Same has {@link Class#isAssignableFrom(Class)} but using class arrays.
     *
     * @param baseTypes    The Class array that check.
     * @param checkedTypes The Class array to be checked.
     * @return             true if checkedTypes can be assigned to baseTypes in respecting order.
     */
    public static boolean isAssignableFrom(@NotNull Class<?>[] baseTypes, @NotNull Class<?>[] checkedTypes) {
        if (baseTypes.length != checkedTypes.length) {
            return false;
        }
        for (int i = 0; i < baseTypes.length; i++) {
            if (!baseTypes[i].isAssignableFrom(checkedTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findGetter(Class, String, Class)} without
     * creating a MethodType, this method require to specify the return type class of {@link Field}
     * and its only compatible with public instance fields, see {@link #staticGetter(Object, Object, String)}
     * for static getter.<br>
     * <p>
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     * <p>
     * See also {@link #unreflectGetter(Object, String)} for private getters.
     *
     * @param clazz      Class to find getter.
     * @param returnType Return type class for provided field name.
     * @param name       Field name.
     * @return A MethodHandle representing a field getter for provided class.
     * @throws NoSuchFieldException   if the field does not exist.
     * @throws IllegalAccessException if access checking fails, or if the field is static.
     */
    @NotNull
    public static MethodHandle getter(@NotNull Object clazz, @NotNull Object returnType, @NotNull String name) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> from = classOf(clazz);
        final Class<?> type = classOf(returnType);
        try {
            return LOOKUP.findGetter(from, name, type);
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                LOGGER.info("unreflectGetter = '" + type.getName() + ' ' + name + "' inside class " + from.getName());
            }
            return unreflectGetter(from, name);
        } catch (NoSuchFieldException e) {
            if (DEBUG) {
                LOGGER.info("findGetter = '" + type.getName() + ' ' + name + "' inside class " + from.getName());
            }
            return unreflectGetter(findField(from, false, type, name));
        }
    }

    /**
     * Same has {@link MethodHandles.Lookup#unreflectGetter(Field)},
     * but this method makes the field accessible if unreflection fails.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param field Field to unreflect.
     * @return      A MethodHandle representing a field getter for provided class.
     * @throws IllegalAccessException if access checking fails.
     */
    @NotNull
    public static MethodHandle unreflectGetter(@NotNull Field field) throws IllegalAccessException {
        try {
            return LOOKUP.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            field.setAccessible(true);
            return LOOKUP.unreflectGetter(field);
        }
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflectGetter(Field)},
     * this method creates an accessible {@link Field} and unreflect it,
     * can be static or instance field.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz Class to find getter.
     * @param name  Field name.
     * @return      A MethodHandle representing a field getter for provided class.
     * @throws NoSuchFieldException   if a field with the specified name is not found.
     * @throws IllegalAccessException if access checking fails.
     */
    @NotNull
    public static MethodHandle unreflectGetter(@NotNull Object clazz, @NotNull String name) throws NoSuchFieldException, IllegalAccessException {
        return LOOKUP.unreflectGetter(field(classOf(clazz), name));
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findStaticGetter(Class, String, Class)} without
     * creating a MethodType, this method require to specify the return type class of {@link Field}
     * and its only compatible with instance fields<br>
     * <p>
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     * <p>
     * See also {@link #unreflectGetter(Object, String)} for private getters.
     *
     * @param clazz      Class to find getter.
     * @param returnType Return type class for provided field name.
     * @param name       Field name.
     * @return A MethodHandle representing a field getter for provided class.
     * @throws NoSuchFieldException   if the field does not exist.
     * @throws IllegalAccessException if access checking fails, or if the field is not static.
     */
    @NotNull
    public static MethodHandle staticGetter(@NotNull Object clazz, @NotNull Object returnType, @NotNull String name) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> from = classOf(clazz);
        final Class<?> type = classOf(returnType);
        try {
            return LOOKUP.findStaticGetter(from, name, type);
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                LOGGER.info("unreflectGetter = 'static " + type.getName() + ' ' + name + "' inside class " + from.getName());
            }
            return unreflectGetter(from, name);
        } catch (NoSuchFieldException e) {
            if (DEBUG) {
                LOGGER.info("unreflectGetter = 'static " + type.getName() + ' ' + name + "' inside class " + from.getName());
            }
            return unreflectGetter(findField(from, true, type, name));
        }
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findSetter(Class, String, Class)} without
     * creating a MethodType, this method require to specify the return type class of {@link Field}
     * and its only compatible with public instance fields, see {@link #staticSetter(Object, Object, String)}
     * for static setter.<br>
     * <p>
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     * <p>
     * See also {@link #unreflectSetter(Object, String)} for private setters.
     *
     * @param clazz      Class to find setter.
     * @param returnType Return type class for provided field name.
     * @param name       Field name.
     * @return A MethodHandle representing a field setter for provided class.
     * @throws NoSuchFieldException   if the field does not exist.
     * @throws IllegalAccessException if access checking fails, or if the field is static.
     */
    @NotNull
    public static MethodHandle setter(@NotNull Object clazz, @NotNull Object returnType, @NotNull String name) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> from = classOf(clazz);
        final Class<?> type = classOf(returnType);
        try {
            return LOOKUP.findSetter(from, name, type);
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                LOGGER.info("unreflectSetter = '" + type.getName() + ' ' + name + "' inside class " + from.getName());
            }
            return unreflectSetter(from, name);
        } catch (NoSuchFieldException e) {
            if (DEBUG) {
                LOGGER.info("findSetter = '" + type.getName() + ' ' + name + "' inside class " + from.getName());
            }
            return unreflectSetter(findField(from, false, type, name));
        }
    }

    /**
     * Same has {@link MethodHandles.Lookup#unreflectSetter(Field)},
     * but this method makes the field accessible if unreflection fails.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param field Field to unreflect.
     * @return      A MethodHandle representing a field setter for provided class.
     * @throws IllegalAccessException if access checking fails.
     */
    @NotNull
    public static MethodHandle unreflectSetter(@NotNull Field field) throws IllegalAccessException {
        try {
            return LOOKUP.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            field.setAccessible(true);
            return LOOKUP.unreflectSetter(field);
        }
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflectSetter(Field)},
     * this method creates a accessible {@link Field} and unreflect it,
     * can be static or instance field.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz Class to find setter.
     * @param name  Field name.
     * @return      A MethodHandle representing a field setter for provided class.
     * @throws NoSuchFieldException   if a field with the specified name is not found.
     * @throws IllegalAccessException if access checking fails.
     */
    @NotNull
    public static MethodHandle unreflectSetter(@NotNull Object clazz, @NotNull String name) throws NoSuchFieldException, IllegalAccessException {
        return LOOKUP.unreflectSetter(field(classOf(clazz), name));
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findStaticSetter(Class, String, Class)} without
     * creating a MethodType, this method require to specify the return type class of {@link Field}
     * and its only compatible with instance fields<br>
     * <p>
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     * <p>
     * See also {@link #unreflectSetter(Object, String)} for private setters.
     *
     * @param clazz      Class to find setter.
     * @param returnType Return type class for provided field name.
     * @param name       Field name.
     * @return A MethodHandle representing a field setter for provided class.
     * @throws NoSuchFieldException   if the field does not exist.
     * @throws IllegalAccessException if access checking fails, or if the field is not static.
     */
    @NotNull
    public static MethodHandle staticSetter(@NotNull Object clazz, @NotNull Object returnType, @NotNull String name) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> from = classOf(clazz);
        final Class<?> type = classOf(returnType);
        try {
            return LOOKUP.findStaticSetter(from, name, type);
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                LOGGER.info("unreflectSetter = 'static " + type.getName() + ' ' + name + "' inside class " + from.getName());
            }
            return unreflectSetter(from, name);
        } catch (NoSuchFieldException e) {
            if (DEBUG) {
                LOGGER.info("findSetter = 'static " + type.getName() + ' ' + name + "' inside class " + from.getName());
            }
            return unreflectSetter(findField(from, true, type, name));
        }
    }

    /**
     * Find field inside class using recursive searching and return it<br>
     *
     * @param from     Class to find the field.
     * @param isStatic True if field is static.
     * @param type     Return type class for provided field name.
     * @param name     Field name.
     * @return A field from provided class.
     * @throws NoSuchFieldException   if the field does not exist.
     * @throws IllegalAccessException if access checking fails, or if the field is not static.
     */
    @NotNull
    public static Field findField(@NotNull Class<?> from, boolean isStatic, @NotNull Class<?> type, @NotNull String name) throws NoSuchFieldException, IllegalAccessException {
        // Find with name
        try {
            final Field field = from.getDeclaredField(name);
            if (Modifier.isStatic(field.getModifiers()) == isStatic && type.isAssignableFrom(field.getType())) {
                return field;
            }
        } catch (NoSuchFieldException ignored) { }
        // Find using field type
        for (Field field : from.getDeclaredFields()) {
            if (field.getType().equals(type)) {
                return field;
            }
        }
        for (Field field : from.getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType())) {
                return field;
            }
        }
        throw new NoSuchFieldException("Cannot find a field like '" + (isStatic ? "static " : "") + type.getName() + ' ' + name + "' inside class " + from.getName());
    }

    /**
     * Get accessible field from provided class.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz Class to find setter.
     * @param field Field name.
     * @return      A field from provided class with access permission.
     * @throws NoSuchFieldException if the field does not exist.
     */
    @NotNull
    public static Field field(@NotNull Object clazz, @NotNull String field) throws NoSuchFieldException {
        Field f = classOf(clazz).getDeclaredField(field);
        f.setAccessible(true);
        return f;
    }

    @NotNull
    private static MethodType type(@NotNull Object returnType, @NotNull Object... classes) {
        return type(classOf(returnType), classesOf(classes));
    }

    @NotNull
    private static MethodType type(@NotNull Class<?> returnType, @NotNull Class<?>... classes) {
        switch (classes.length) {
            case 0:
                return MethodType.methodType(returnType);
            case 1:
                return MethodType.methodType(returnType, classes[0]);
            default:
                return MethodType.methodType(returnType, classes[0], Arrays.copyOfRange(classes, 1, classes.length));
        }
    }

    @NotNull
    private static String[] names(@NotNull Class<?>[] classes) {
        final String[] names = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            names[i] = classes[i].getName();
        }
        return names;
    }

    /**
     * Boolean valued function to compare a method with return and parameter types.
     *
     * @author Rubenicos
     */
    @FunctionalInterface
    public interface MethodPredicate {

        /**
         * Eval this predicate with current arguments.
         *
         * @param method         method to compare.
         * @param returnType     method return type.
         * @param parameterTypes method parameter types as array.
         * @return               true if method matches.
         */
        boolean test(@NotNull Method method, @NotNull Class<?> returnType, @NotNull Class<?>[] parameterTypes);
    }
}
