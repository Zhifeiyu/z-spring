package com.z.springframework.annotation;

import java.lang.annotation.*;

/**
 * @author zfylin
 * @version 2020/09/15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface Service {
    String value() default "";
}