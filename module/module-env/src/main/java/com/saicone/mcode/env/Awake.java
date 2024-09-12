package com.saicone.mcode.env;

import com.saicone.mcode.platform.PlatformType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Documented
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Awake {

    Executes when();

    PlatformType[] platform() default {};

    int priority() default 0;

    long delay() default 0;

    long period() default 0;

    TimeUnit unit() default TimeUnit.SECONDS;

    String[] condition() default {};

    String[] dependsOn() default {};

}
