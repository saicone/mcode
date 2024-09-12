package com.saicone.mcode.bootstrap;

import com.saicone.mcode.platform.PlatformType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface PluginDescription {

    // Extra

    String[] header() default {};

    String[] extra() default {};


    // Information

    String id() default ""; // Optional velocity support

    String name();

    String[] aliases() default {};

    String prefix() default "";

    String description() default "";

    String version();

    String[] authors() default {};

    String[] contributors() default {};

    String website() default "";


    // Load

    PlatformType[] platform() default {};

    Addon[] addons() default {};

    String main() default "";

    String bootstrapper() default "";

    String loader() default "";


    // Behaviour

    String compatibility() default "";

    // STARTUP | POSTWORLD
    String load() default "";

    boolean foliaSupported() default false;

    boolean openClassLoader() default false;


    // Dependencies

    String[] depend() default {};

    String[] softDepend() default {};

    String[] loadBefore() default {};

    PluginDependencies[] dependencies() default {};
}
