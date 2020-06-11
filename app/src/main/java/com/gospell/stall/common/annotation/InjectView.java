package com.gospell.stall.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectView {
    int id() default -1;

    //view 的 layout
    int layout() default -1;

    Class<?>[] listeners() default {};

}
