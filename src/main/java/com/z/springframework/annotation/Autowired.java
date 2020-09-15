package com.z.springframework.annotation;

import java.lang.annotation.*;

/**
 * @author zfylin
 * @version 2020/09/15
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    String value() default "";
}
