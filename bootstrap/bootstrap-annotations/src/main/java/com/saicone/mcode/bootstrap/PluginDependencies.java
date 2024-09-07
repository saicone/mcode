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
public @interface PluginDependencies {

    PluginDependency[] value();

    PlatformType[] platform() default {};

    // bootstrap | server
    String section() default "";
}
