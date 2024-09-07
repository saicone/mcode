package com.saicone.mcode.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Repeatable(PluginDependencies.class)
public @interface PluginDependency {

    String id() default ""; // Optional Velocity support

    String value();

    boolean required() default true;

    boolean optional() default false;

    // BEFORE | AFTER | OMIT
    String load() default "";

    boolean joinClasspath() default true;
}
