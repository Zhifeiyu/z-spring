package com.z.springframework.beans.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author zfylin
 * @version 2020/09/15
 */
@Setter
@Getter
@NoArgsConstructor
public class BeanDefinition {

    private String beanClassName;

    private boolean lazyInit = false;

    private String factoryBeanName;
}
